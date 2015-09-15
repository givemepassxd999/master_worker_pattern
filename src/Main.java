import java.util.Map;

public class Main {
    public static void main(String[] args){
        Master master = new Master(new StringWorker("-1"), 5);
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


