package cc.concurrent.conan;

import cc.concurrent.conan.core.MsgQueue;
import cc.concurrent.conan.core.consumer.Consumer;
import cc.concurrent.conan.core.consumer.RemoteConsumer;
import cc.concurrent.conan.util.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
* User: yanghe.liang
* Date: 13-8-22
* Time: 下午4:20
*/
public class MsgExecutor {

    private volatile MsgQueue msgQueue; // 保证能初始化完全

    private final static int DEFAULT_MAX_BATCH_NUM = 100;
    private final static int DEFAULT_MAX_BATCH_SIZE = 1000;

    private final static int DEFAULT_MAX_ERROR_NUM = 10;
    private final static long DEFAULT_TIME_SLICE = 5000L;

    private MsgExecutor(Consumer consumer, int threadNum, int maxBatchNum, int maxBatchSize,
                        int maxErrorNum, long timeSlice, ThreadFactory threadFactory) {
        msgQueue = new MsgQueue(consumer, threadNum, maxBatchNum, maxBatchSize,
                maxErrorNum, timeSlice, threadFactory);
        msgQueue.start();
    }

    public boolean handle(Msg msg) {
        return msgQueue.handle(msg);
    }

    public QueueStats snapshot() {
        return msgQueue.snapshot();
    }

    public static MsgExecutor remoteExecutor(String host, int port, String path, String parameter) {
        return remoteExecutor(host, port, path, parameter, 1);
    }

    public static MsgExecutor remoteExecutor(String host, int port, String path, String parameter, int threadNum) {
        return create(RemoteConsumer.create(host, port, path, parameter), threadNum,
                DEFAULT_MAX_BATCH_NUM, DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_ERROR_NUM, DEFAULT_TIME_SLICE,
                new DefaultThreadFactory());
    }

    public static MsgExecutor create(Consumer consumer, int threadNum, int maxBatchNum, int maxBatchSize,
                                     int maxErrorNum, long timeSlice, ThreadFactory threadFactory) {
        return new MsgExecutor(consumer, threadNum, maxBatchNum, maxBatchSize,
                maxErrorNum, timeSlice, threadFactory);
    }

    public static MsgExecutor create(Consumer consumer, int threadNum) {
        return create(consumer, threadNum,
                DEFAULT_MAX_BATCH_NUM, DEFAULT_MAX_BATCH_SIZE, DEFAULT_MAX_ERROR_NUM, DEFAULT_TIME_SLICE,
                new DefaultThreadFactory());
    }

}
