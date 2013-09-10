package cc.concurrent.conan.core;

import cc.concurrent.conan.Msg;
import cc.concurrent.conan.QueueStats;
import cc.concurrent.conan.core.consumer.Consumer;
import cc.concurrent.conan.util.logging.InternalLogger;
import cc.concurrent.conan.util.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static cc.concurrent.conan.util.Preconditions.checkArgument;
import static cc.concurrent.conan.util.Preconditions.checkNotNull;
import static cc.concurrent.conan.util.Preconditions.checkState;

/**
 * 消息队列，生产者消费者模型
 * User: yanghe.liang
 * Date: 13-7-14
 * Time: 上午8:41
 */
public class MsgQueue {

    private volatile Consumer consumer; // 消息处理逻辑
    private final BlockingQueue<Msg> workQueue; // 处理消息的队列
    private final int threadNum; // 处理消息的线程数
    private final int maxBatchNum; // 最大批量取消息数
    private final int maxBatchSize; // 最大批量取消息大小
    private final int maxErrorNum; // 最大连续出错数
    private final long timeSlice; // 消息队列为空时，取消息阻塞时间
    private final long waitPark; // 检测其他线程是否挂起周期
    private final long waitCheck; // 检测消息处理逻辑是否正常周期
    private final ThreadFactory threadFactory; // 线程工厂

    private Thread[] threads = null; // 处理消息的线程
    private final AtomicInteger parkThreadNum = new AtomicInteger(0); // 出错后，线程挂起数
    private final AtomicInteger errorNum = new AtomicInteger(0); // 连续出错数
    private final AtomicBoolean errorFlag = new AtomicBoolean(false); // 错误处理标志位

    private final AtomicLong totalErrorNum = new AtomicLong(0); // 统计出错总数
    private final AtomicLong totalMsgNum = new AtomicLong(0); // 统计处理消息总数

    private final ReentrantLock lock = new ReentrantLock();
    private volatile State state = State.NEW;

    private final InternalLogger logger = InternalLoggerFactory.getLogger(MsgQueue.class);

    public MsgQueue(Consumer consumer, BlockingQueue<Msg> workQueue, int threadNum, int maxBatchNum, int maxBatchSize,
                    int maxErrorNum, long timeSlice, long waitPark, long waitCheck, ThreadFactory threadFactory) {
        checkNotNull(consumer, "consumer can't be null");
        checkNotNull(workQueue, "workQueue can't be null");
        checkArgument(threadNum > 0, "threadNum must larger than zero");
        checkArgument(maxBatchNum > 0, "maxBatchNum must larger than zero");
        checkArgument(maxBatchSize > 0, "maxBatchSize must larger than zero");
        checkArgument(maxErrorNum > 0, "maxErrorNum must larger than zero");
        checkArgument(timeSlice > 0, "timeSlice must larger than zero");
        checkArgument(waitPark > 0, "waitPark must larger than zero");
        checkArgument(waitCheck > 0, "waitCheck must larger than zero");
        checkNotNull(threadFactory, "threadFactory can't be null");

        this.consumer = consumer;
        this.workQueue = workQueue;
        this.threadNum = threadNum;
        this.maxBatchNum = maxBatchNum;
        this.maxBatchSize = maxBatchSize;
        this.maxErrorNum = maxErrorNum;
        this.timeSlice = timeSlice;
        this.waitPark = waitPark;
        this.waitCheck = waitCheck;
        this.threadFactory = threadFactory;
    }

    public void start() {
        lock.lock();
        try {
            checkState(state == State.NEW || state == State.STOP, "state need NEW or STOP but %s", state.toString());

            parkThreadNum.set(0);
            errorNum.set(0);
            errorFlag.set(false);
            totalErrorNum.set(0);
            totalMsgNum.set(0);
            workQueue.clear(); // 清空所有消息

            threads = new Thread[threadNum];
            for (int i = 0; i < threadNum; i++) {
                threads[i] = threadFactory.newThread(new Dispatcher());
            }
            for (int i = 0; i < threadNum; i++) {
                threads[i].start();
            }

            state = State.START;
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        lock.lock();
        try {
            checkState(state == State.START, "state need START but %s", state.toString());
            checkNotNull(threads);

            state = State.STOP;

            // 中断运行中线程
            for (int i = 0; i < threads.length; i++) {
                threads[i].interrupt();
            }

            // 等待线程结束
            for (int i = 0; i < threads.length; i++) {
                threads[i].join(); // 如果在join时发生中断，则队列处于FAIL状态
            }
            threads = null;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            state = State.FAIL;
        } finally {
            lock.unlock();
        }
    }

    public boolean handle(Msg msg) {
        checkNotNull(msg, "msg can't be null");
        checkArgument(!msg.isEmpty(), "msg must have data");
        checkArgument(msg.size() <= maxBatchNum, "max one msg size is %s but %s", maxBatchNum, msg.size());

        if (state != State.START) {
            return false;
        }

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
        while (!isInterrupted()) {
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
                Thread.currentThread().interrupt();   // 设置中断标志
            }
        }
        return msgs;
    }

    /**
     * 将消息发送到远程服务器
     */
    private void handle(List<Msg> msgs) {
        int t = 0;
        while (!isInterrupted()) {
            t++;
            if (logger.isDebugEnabled()) {
                logger.debug("%s handle %s times, %s msgs %s", Thread.currentThread().getName(), t, msgs.size(), msgs);
            }
            if (consumer.handle(msgs)) {
                totalMsgNum.addAndGet(msgs.size()); // 统计处理消息总数
                if (errorFlag.get()) {
                    park();
                } else {
                    errorNum.set(0);
                }
                break;
            } else { // 失败
                totalErrorNum.incrementAndGet(); // 统计出错总数
                if (errorFlag.get()) {
                    park();
                } else if (errorNum.incrementAndGet() > maxErrorNum) { // 超出最大错误次数
                    if (errorFlag.compareAndSet(false, true)) { // 抢占出错监控
                        checkUntilOk();
                        errorNum.set(0);
                        errorFlag.set(false);
                        logger.error("%s unpark other thread", Thread.currentThread().getName());
                        for (int i = 0; i < threads.length; i++) {
                            if (Thread.currentThread() != threads[i]) {
                                unpark(threads[i]);
                            }
                        }
                    } else {
                        park();
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
            LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(waitPark));
        }
        while (!isInterrupted()) {
            if (consumer.check()) {
                break;
            }
            logger.error("%s wait remote ok", Thread.currentThread().getName());
            LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(waitCheck));
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

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    private static boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    private class Dispatcher implements Runnable {
        public void run() {
            while (!isInterrupted()) {
                List<Msg> msgs = batchPoll();
                if (errorFlag.get()) {
                    park();
                }
                if (!msgs.isEmpty()) {
                    handle(msgs);
                }
            }
            logger.info("%s stop over", Thread.currentThread().getName());
        }
    }

    enum State {
        NEW, START, STOP, FAIL
    }

}

