package cc.concurrent.conan;

import cc.concurrent.conan.core.MsgQueue;
import cc.concurrent.conan.core.consumer.Consumer;
import cc.concurrent.conan.core.consumer.RemoteConsumer;
import cc.concurrent.conan.util.DefaultThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
* User: yanghe.liang
* Date: 13-8-22
* Time: 下午4:20
*/
public class MsgExecutor {

    private MsgQueue msgQueue; // 真正执行操作的类

    public final static int DEFAULT_QUEUE_CAPACITY = 1024 * 100;
    private final static int DEFAULT_MAX_BATCH_NUM = 100;
    private final static int DEFAULT_MAX_BATCH_SIZE = 1000;
    private final static int DEFAULT_MAX_ERROR_NUM = 10;
    private final static long DEFAULT_TIME_SLICE = 5000L;
    private final static long DEFAULT_WAIT_PARK = 1000L;
    private final static long DEFAULT_WAIT_CHECK = 2000L;


    private MsgExecutor(Consumer consumer, BlockingQueue<Msg> workQueue, int threadNum, int maxBatchNum, int maxBatchSize,
                        int maxErrorNum, long timeSlice, long waitPark, long waitCheck, ThreadFactory threadFactory) {
        msgQueue = new MsgQueue(consumer, workQueue, threadNum, maxBatchNum, maxBatchSize,
                maxErrorNum, timeSlice, waitPark, waitCheck, threadFactory);
        msgQueue.start();
    }

    public boolean handle(Msg msg) {
        return msgQueue.handle(msg);
    }

    public void stop() {
        msgQueue.stop();
    }

    public void start() {
        msgQueue.start();
    }

    public QueueStats snapshot() {
        return msgQueue.snapshot();
    }

    public static MsgExecutor remoteExecutor(String host, int port, String path, String parameter) {
        return remoteExecutor(host, port, path, parameter, new ArrayBlockingQueue<Msg>(DEFAULT_QUEUE_CAPACITY), 1);
    }

    public static MsgExecutor remoteExecutor(String host, int port, String path, String parameter, BlockingQueue<Msg> workQueue, int threadNum) {
        return create(RemoteConsumer.create(host, port, path, parameter), workQueue, threadNum);
    }

    public static MsgExecutor create(Consumer consumer, BlockingQueue<Msg> workQueue, int threadNum) {
        return create(consumer, workQueue, threadNum, DEFAULT_MAX_BATCH_NUM, DEFAULT_MAX_BATCH_SIZE,
                DEFAULT_MAX_ERROR_NUM, DEFAULT_TIME_SLICE, DEFAULT_WAIT_PARK, DEFAULT_WAIT_CHECK, new DefaultThreadFactory());
    }

    public static MsgExecutor create(Consumer consumer, BlockingQueue<Msg> workQueue, int threadNum, int maxBatchNum, int maxBatchSize,
                                     int maxErrorNum, long timeSlice, long waitPark, long waitCheck, ThreadFactory threadFactory) {
        return new MsgExecutor(consumer, workQueue, threadNum, maxBatchNum, maxBatchSize,
                maxErrorNum, timeSlice, waitPark, waitCheck, threadFactory);
    }

}
