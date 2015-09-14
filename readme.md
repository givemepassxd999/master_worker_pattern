Master-Worker是一個很好用的設計模式,
它可以把一個大工作分成很多小工作去執行, 等所有小工作都回來以後,
將所有的小工作結果做一個統整, 得到最終結果,
就很像專題小組有五個人, 把系統分成四等分, 每個人負責一份, 
最後那個人負責統整, 會比只有一個人處理整個系統還要有效率。

![](https://dl.dropboxusercontent.com/u/24682760/Android_AS/MasterWorkerPattern/master_worker_pattern.png)

從上圖可以得知, master是負責協調任務跟整合結果的中繼者,
所以我們試著寫出該設計模式。
```
題目: 在一個很大的字串陣列內, 找出那些字串含有"1"的元素。

解法: 先將每個陣列元素視為一個Worker處理的task, 再透過每個Worker分工找出字串, 
      結果回傳至結果區, 繼續找下一個task, 直到所有task都結束。
```  
master程式碼

```java
public class Master {
    protected Queue<Object> mWorkQueue;
    protected List<Thread> mThreadList;
    protected Map<String, Object> mResultMap;

    public boolean isComplete() {
        for (Thread t : mThreadList) {
            if (t.getState() != Thread.State.TERMINATED) {
                return false;
            }
        }
        return true;
    }

    public Master(Worker worker, int countWorker) {
        mWorkQueue = new ConcurrentLinkedQueue<Object>();
        mResultMap = new ConcurrentHashMap<String, Object>();
        mThreadList = new ArrayList<Thread>();

        worker.setWorkQueue(mWorkQueue);
        worker.setResultMap(mResultMap);
        for (int i = 0; i < countWorker; i++) {
            mThreadList.add(new Thread(worker, Integer.toString(i)));
        }
    }

    public void addTask(Object job) {
        mWorkQueue.add(job);
    }

    public Map<String, Object> getResult() {
        return mResultMap;
    }

    public void execute() {
        for (Thread t : mThreadList) {
            t.start();
        }
    }
}
```
建構子把一個Worker丟進來, 包成一個Thread放進Thread Array。

宣告一個Queue, 所有底下的工作者, 都會存取這個Queue, 由於一次只能有一個worker取出, 避免race conditiion,
因此宣告成Concurrent, 又由於先進先出的特性, 因此會宣告成Queue。

再來宣告一個一個Map, 理由同上, 由於所有工作者都會把計算好的結果丟進Map, 因此只允許一次一個Worker進行存取,
所以宣告成Concurrent, Map是用來存放所有結果的區域。

那我們會將每個要找的字串(或物件)透過addTask存放到Queue內, 等到所有工作完成, 則可以回傳Map, 
因此會多宣告一個方法來進行判斷是否全部task都完成了。

Worker程式碼

```java
public abstract class Worker implements Runnable{
    protected Queue<Object> mWorkQueue;
    protected Map<String, Object> mResultMap;

    public void setWorkQueue(Queue<Object> workQueue) {
        this.mWorkQueue = workQueue;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.mResultMap = resultMap;
    }

    //subclass handle task
    public abstract Object handle(Object task);

    @Override
    public void run() {
        while (true) {
            Object input = mWorkQueue.poll();
            if (input == null) {
                break;
            }
            Object re = handle(input);
            mResultMap.put(Integer.toString(input.hashCode()), re);
        }
    }
}
```
Worker就是用來處理每個小任務的人, 一開始會有兩個方法, 一個是取得Queue, 這樣才能拿到每一個task,
另外一個方法就是取得Map, 這樣才可以把算完的結果丟回去,
那至於run就是我們要從Queue取出任務, 執行完再塞回Map的部分,
至於handle方法不知道, 因為這是一個虛擬類別, 所以真正的worker才會知道要處理怎樣的任務,
因此我們必須繼承這個類別, 並且覆寫handle這個方法。

Task的程式碼

```java
public class MyString{
    private String str;

    private boolean isFind;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public boolean isFind() {
        return isFind;
    }

    public void setIsFind(boolean isFind) {
        this.isFind = isFind;
    }
}
```
我們任務裝的物件很簡單, 就是一個字串, 以及這個結果是否有被找到。

再來就是我們實際Worker的程式碼了。

```java
public class StringWorker extends Worker {

    private String mSearchText;

    public StringWorker(String searchText){
        mSearchText = searchText;
    }

    @Override
    public Object handle(Object task) {
        MyString strObj = new MyString();
        if(task == null){
            return strObj;
        }
        if(task instanceof MyString){
            strObj = (MyString) task;
            String s = strObj.getStr();
            if(s.indexOf(mSearchText) != -1){
                strObj.setIsFind(true);
            } else{
                strObj.setIsFind(false);
            }
        }
        return strObj;
    }
}
```
他覆寫了Worker類別, 在handle方法內, 將task拆解開來, 從建構子傳入要尋找的字串進行比對,
將結果塞入task的物件並且回傳。

主程式程式碼

```java
public class Main {
    public static void main(String[] args){
        Master master = new Master(new StringWorker("1"), 5);
        for (int i = 0; i < 20; i++) {
            MyString ms = new MyString();
            ms.setStr(String.valueOf(i));
            ms.setIsFind(false);
            master.addTask(ms);
        }
        master.execute();

        while (!master.isComplete()) ;
        Map<String, Object> resultMap = master.getResult();
        for(String key : resultMap.keySet()){
            MyString ms = ((MyString) resultMap.get(key));
            System.out.println("name:" + ms.getStr());
            System.out.println("is find:" + ms.isFind());
        }
    }
}
```
在此先做一個小範圍的實驗, 即20筆資料即可, 利用Master建構子宣告出5個Worker,
並且希望工作者找出task內是否有"1"字串存在,
在for迴圈內, 是用來初始化所有的工作, 以index做為資料的內容,
最後將每個task透過master加入到Queue內部, 最後執行master內所有的Worker。

在我們所有任務沒有完成之前, 會讓master一直空轉在while迴圈上面,
如果是在Android建議將此部分放入到Thread, 避免user無法進行操作。

等到所有任務完成, 則把每個Task的name跟isFind都印出。
```java
name:8
is find:false
name:13
is find:true
name:2
is find:false
name:15
is find:true
name:12
is find:true
name:1
is find:true
name:18
is find:true
name:14
is find:true
name:10
is find:true
name:5
is find:false
name:9
is find:false
name:0
is find:false
name:11
is find:true
name:3
is find:false
name:17
is find:true
name:4
is find:false
name:7
is find:false
name:19
is find:true
```

你會看到每個任務完成的時間不一定, 所以每次執行的結果順序也都不一致。
以上就是有效率的Master-Worker Pattern。
[程式碼](https://dl.dropboxusercontent.com/u/24682760/Android_AS/MasterWorkerPattern/Master_Worker_Pattern.zip)