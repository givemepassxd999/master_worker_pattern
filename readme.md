Master-Worker�O�@�ӫܦn�Ϊ��]�p�Ҧ�,
���i�H��@�Ӥj�u�@�����ܦh�p�u�@�h����, ���Ҧ��p�u�@���^�ӥH��,
�N�Ҧ����p�u�@���G���@�Ӳξ�, �o��̲׵��G,
�N�ܹ��M�D�p�զ����ӤH, ��t�Τ����|����, �C�ӤH�t�d�@��, 
�̫ᨺ�ӤH�t�d�ξ�, �|��u���@�ӤH�B�z��Өt���٭n���Ĳv�C

![](https://dl.dropboxusercontent.com/u/24682760/Android_AS/MasterWorkerPattern/master_worker_pattern.png)

�q�W�ϥi�H�o��, master�O�t�d��ե��ȸ��X���G�����~��,
�ҥH�ڭ̸յۼg�X�ӳ]�p�Ҧ��C
```
�D��: �b�@�ӫܤj���r��}�C��, ��X���Ǧr��t��"1"�������C

�Ѫk: ���N�C�Ӱ}�C���������@��Worker�B�z��task, �A�z�L�C��Worker���u��X�r��, 
      ���G�^�Ǧܵ��G��, �~���U�@��task, ����Ҧ�task�������C
```  
master�{���X

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
�غc�l��@��Worker��i��, �]���@��Thread��iThread Array�C

�ŧi�@��Queue, �Ҧ����U���u�@��, ���|�s���o��Queue, �ѩ�@���u�঳�@��worker���X, �קKrace conditiion,
�]���ŧi��Concurrent, �S�ѩ���i���X���S��, �]���|�ŧi��Queue�C

�A�ӫŧi�@�Ӥ@��Map, �z�ѦP�W, �ѩ�Ҧ��u�@�̳��|��p��n�����G��iMap, �]���u���\�@���@��Worker�i��s��,
�ҥH�ŧi��Concurrent, Map�O�ΨӦs��Ҧ����G���ϰ�C

���ڭ̷|�N�C�ӭn�䪺�r��(�Ϊ���)�z�LaddTask�s���Queue��, ����Ҧ��u�@����, �h�i�H�^��Map, 
�]���|�h�ŧi�@�Ӥ�k�Ӷi��P�_�O�_����task�������F�C

Worker�{���X

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
Worker�N�O�ΨӳB�z�C�Ӥp���Ȫ��H, �@�}�l�|����Ӥ�k, �@�ӬO���oQueue, �o�ˤ~�ள��C�@��task,
�t�~�@�Ӥ�k�N�O���oMap, �o�ˤ~�i�H��⧹�����G��^�h,
���ܩ�run�N�O�ڭ̭n�qQueue���X����, ���槹�A��^Map������,
�ܩ�handle��k�����D, �]���o�O�@�ӵ������O, �ҥH�u����worker�~�|���D�n�B�z��˪�����,
�]���ڭ̥����~�ӳo�����O, �åB�мghandle�o�Ӥ�k�C

Task���{���X

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
�ڭ̥��ȸ˪������²��, �N�O�@�Ӧr��, �H�γo�ӵ��G�O�_���Q���C

�A�ӴN�O�ڭ̹��Worker���{���X�F�C

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
�L�мg�FWorker���O, �bhandle��k��, �Ntask��Ѷ}��, �q�غc�l�ǤJ�n�M�䪺�r��i����,
�N���G��Jtask������åB�^�ǡC

�D�{���{���X

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
�b�������@�Ӥp�d�򪺹���, �Y20����ƧY�i, �Q��Master�غc�l�ŧi�X5��Worker,
�åB�Ʊ�u�@�̧�Xtask���O�_��"1"�r��s�b,
�bfor�j�餺, �O�ΨӪ�l�ƩҦ����u�@, �Hindex������ƪ����e,
�̫�N�C��task�z�Lmaster�[�J��Queue����, �̫����master���Ҧ���Worker�C

�b�ڭ̩Ҧ����ȨS���������e, �|��master�@������bwhile�j��W��,
�p�G�O�bAndroid��ĳ�N��������J��Thread, �קKuser�L�k�i��ާ@�C

����Ҧ����ȧ���, �h��C��Task��name��isFind���L�X�C
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

�A�|�ݨ�C�ӥ��ȧ������ɶ����@�w, �ҥH�C�����檺���G���Ǥ]�����@�P�C
�H�W�N�O���Ĳv��Master-Worker Pattern�C
[�{���X](https://dl.dropboxusercontent.com/u/24682760/Android_AS/MasterWorkerPattern/Master_Worker_Pattern.zip)