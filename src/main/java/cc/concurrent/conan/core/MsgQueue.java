package cc.concurrent.conan.core;

import cc.concurrent.conan.Msg;
import cc.concurrent.conan.QueueStats;
import cc.concurrent.conan.core.consumer.Consumer;
import cc.concurrent.conan.util.logging.InternalLogger;
import cc.concurrent.conan.util.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static cc.concurrent.conan.util.Preconditions.checkArgument;
import static cc.concurrent.conan.util.Preconditions.checkNotNull;

/**
 * 消息队列，生产者消费者模型
 * User: yanghe.liang
 * Date: 13-7-14
 * Time: 上午8:41
 */
public class MsgQueue {

    private final BlockingQueue<Msg> workQueue = new LinkedBlockingQueue<Msg>();

    private volatile Consumer consumer; // 可以在队列运行后随时变化
    private final int maxBatchNum;
    private final int maxBatchSize;
    private final int threadNum;

    private final int maxErrorNum;
    private final long timeSlice;
    private final ThreadFactory threadFactory;

    private Thread[] threads = null;
    private AtomicInteger parkThreadNum = new AtomicInteger(0);
    private final AtomicInteger errorNum = new AtomicInteger(0);
    private final AtomicBoolean errorFlag = new AtomicBoolean(false);

    private final AtomicLong totalErrorNum = new AtomicLong(0); // 统计出错总数
    private final AtomicLong totalMsgNum = new AtomicLong(0); // 统计处理消息总数

    private final ReentrantLock lock = new ReentrantLock();

    private final InternalLogger logger = InternalLoggerFactory.getLogger(MsgQueue.class);

    public MsgQueue(Consumer consumer, int threadNum, int maxBatchNum, int maxBatchSize,
                    int maxErrorNum, long timeSlice, ThreadFactory threadFactory) {

        checkNotNull(consumer, "consumer can't be null");
        checkArgument(threadNum > 0, "threadNum must larger than zero");
        checkArgument(maxBatchNum > 0, "maxBatchNum must larger than zero");
        checkArgument(maxBatchSize > 0, "maxBatchSize must larger than zero");
        checkArgument(maxErrorNum > 0, "maxErrorNum must larger than zero");
        checkArgument(timeSlice > 0, "timeSlice must larger than zero");
        checkNotNull(threadFactory, "threadFactory can't be null");

        this.consumer = consumer;
        this.maxBatchNum = maxBatchNum;
        this.maxBatchSize = maxBatchSize;
        this.threadNum = threadNum;
        this.maxErrorNum = maxErrorNum;
        this.timeSlice = timeSlice;
        this.threadFactory = threadFactory;
    }

    public void start() {
        lock.lock();
        try {
            if (threads != null) {
                throw new IllegalStateException("msgQueue has started");
            }
            threads = new Thread[threadNum];
            for (int i = 0; i < threadNum; i++) {
                threads[i] = threadFactory.newThread(new Dispatcher());
            }
            for (int i = 0; i < threadNum; i++) {
                threads[i].start();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean handle(Msg msg) {
        checkNotNull(msg, "msg can't be null");
        checkArgument(!msg.isEmpty(), "msg must have data");
        checkArgument(msg.size() <= maxBatchNum, "max one msg size is %s but %s", maxBatchNum, msg.size());

        if (logger.isDebugEnabled()) {
            logger.debug("add msg %s", msg);
        }
        return workQueue.offer(msg);
    }

    public QueueStats snapshot() {
        return new QueueStats(totalMsgNum.get(), totalErrorNum.get(), workQueue.size());
    }

    /**
     * 从消息队列中批量取数据
     * @return
     */
    private List<Msg> batchPoll() {
        List<Msg> msgs = new ArrayList<Msg>();
        int size = 0;
        while (true) {
            try {
                Msg msg = msgs.isEmpty() ?
                        workQueue.poll(timeSlice, TimeUnit.MILLISECONDS) :
                        workQueue.poll();
                if (msg == null) { // 没有消息，直接结束循环
                    break;
                }
                msgs.add(msg);
                size += msg.size();
                if (size >= maxBatchSize || msgs.size() >= maxBatchNum) {
                    break;
                }
            } catch (InterruptedException ie) {
                // 被中断
                break;
            }
        }
        return msgs;
    }

    /**
     * 将消息发送到远程服务器
     */
    private void handle(List<Msg> msgs) {
        int t = 0;
        while (true) {
            t++;
            if (logger.isDebugEnabled()) {
                logger.debug("%s handle %s times, %s msgs %s", Thread.currentThread().getName(), t, msgs.size(), msgs);
            }
            if (consumer.handle(msgs)) {
                totalMsgNum.addAndGet(msgs.size()); // 统计处理消息总数
                if (errorFlag.get()) {
                    this.park();
                } else {
                    errorNum.set(0);
                }
                break;
            } else { // 失败
                totalErrorNum.incrementAndGet(); // 统计出错总数
                if (errorFlag.get()) {
                    this.park();
                } else if (errorNum.incrementAndGet() > maxErrorNum) { // 超出最大错误次数
                    if (errorFlag.compareAndSet(false, true)) { // 抢占出错监控
                        checkUntilOk();
                        errorNum.set(0);
                        errorFlag.set(false);
                        logger.error("%s unpark other thread", Thread.currentThread().getName());
                        for (int i = 0; i < threads.length; i++) {
                            if (Thread.currentThread() != threads[i]) {
                                this.unpark(threads[i]);
                            }
                        }
                    } else {
                        this.park();
                    }
                }
            }
        }
    }

    /**
     * 监控，直到能重新连接
     */
    private void checkUntilOk() {
        logger.error("%s error to check", Thread.currentThread().getName());
        while (parkThreadNum.get() < threadNum - 1) { // 等待其他线程park
            logger.error("%s wait other thread park", Thread.currentThread().getName());
            LockSupport.parkNanos(this, TimeUnit.SECONDS.toNanos(1L));
        }
        while (true) {
            if (consumer.check()) {
                break;
            }
            logger.error("%s wait remote ok", Thread.currentThread().getName());
            LockSupport.parkNanos(this, TimeUnit.SECONDS.toNanos(2L));
        }
    }

    private void park() {
        parkThreadNum.incrementAndGet();
        logger.error("%s error to park", Thread.currentThread().getName());
        LockSupport.park(this);
    }

    private void unpark(Thread thread) {
        LockSupport.unpark(thread);
    }

    private class Dispatcher implements Runnable {
        public void run() {
            while (true) {
                List<Msg> msgs = batchPoll();
                if (errorFlag.get()) {
                    park();
                }
                if (!msgs.isEmpty()) {
                    handle(msgs);
                }
            }
        }
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

}

