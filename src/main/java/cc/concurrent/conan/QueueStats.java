package cc.concurrent.conan;

/**
 * User: yanghe.liang
 * Date: 13-9-3
 * Time: 下午8:14
 */
public class QueueStats {

    private final long msgNum;
    private final long errorNum;
    private final long queueSize;

    public QueueStats(long msgNum, long errorNum, long queueSize) {
        this.msgNum = msgNum;
        this.errorNum = errorNum;
        this.queueSize = queueSize;
    }

    public long getMsgNum() {
        return msgNum;
    }

    public long getErrorNum() {
        return errorNum;
    }

    public long getQueueSize() {
        return queueSize;
    }

    @Override
    public String toString() {
        return String.format("{msgNum=%s, errorNum=%s, queueSize=%s}", msgNum, errorNum, queueSize);
    }

}
