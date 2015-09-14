import java.util.Map;
import java.util.Queue;

public abstract class Worker implements Runnable{
    protected Queue<Object> mWorkQueue;
    protected Map<String, Object> mResultMap;

    public void setWorkQueue(Queue<Object> workQueue) {
        this.mWorkQueue = workQueue;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.mResultMap = resultMap;
    }

    //subclass hanlde task
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
