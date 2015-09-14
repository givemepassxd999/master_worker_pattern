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
