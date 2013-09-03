package cc.concurrent.conan.core.consumer;

import cc.concurrent.conan.Msg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: yanghe.liang
 * Date: 13-8-28
 * Time: 上午10:29
 */
public class DebugConsumer implements Consumer {

    private final ConcurrentLinkedQueue<Msg> queue = new ConcurrentLinkedQueue<Msg>();

    private volatile boolean succ = true;

    private DebugConsumer() {
    }

    @Override
    public boolean handle(List<Msg> msgs) {
        if (succ) {
            queue.addAll(msgs);
            return true;
        }
        return false;
    }

    @Override
    public boolean check() {
        return succ;
    }

    public int size() {
        return queue.size();
    }

    public List<Msg> getMsgs() {
        List<Msg> msgs = new ArrayList<Msg>();
        for (Msg msg : queue) {
            msgs.add(msg);
        }
        return msgs;
    }

    public static DebugConsumer create() {
        return new DebugConsumer();
    }

    public boolean isSucc() {
        return succ;
    }

    public void setSucc(boolean succ) {
        this.succ = succ;
    }

}
