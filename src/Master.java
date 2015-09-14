import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
