package cc.concurrent.conan;

import cc.concurrent.conan.core.consumer.DebugConsumer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * User: yanghe.liang
 * Date: 13-8-29
 * Time: 下午3:51
 */
public class MsgExecutorTest {

    @BeforeClass
    public static void beforeClass() {
        //InternalLoggerFactory.setDefaultFactory(new Slf4jLoggerFactory());
    }

    @Test
    public void testCreate() throws Exception {
        DebugConsumer dc = DebugConsumer.create();
        MsgExecutor me = MsgExecutor.create(dc, 1);
        List<Msg> msgs = new ArrayList<Msg>();
        for (int i = 0; i < 1000; i++) {
            msgs.add(Msg.create().put("key" + i, "value" + i));
        }
        for (Msg msg : msgs) {
            me.handle(msg);
        }
        while (dc.size() != msgs.size());

        List<Msg> handleMsgs = dc.getMsgs();
        for (int i = 0; i < msgs.size(); i++) {
            assertThat(msgs.get(i), equalTo(handleMsgs.get(i)));
        }
        assertThat(1000L, equalTo(me.snapshot().getMsgNum()));
    }

    @Test
    public void testCreate2() throws Exception {
        DebugConsumer dc = DebugConsumer.create();
        MsgExecutor me = MsgExecutor.create(dc, 2);
        List<Msg> msgs = new ArrayList<Msg>();
        for (int i = 0; i < 1000; i++) {
            msgs.add(Msg.create().put("key" + i, "value" + i));
        }
        for (Msg msg : msgs) {
            me.handle(msg);
        }
        while (dc.size() != msgs.size());
        List<Msg> handleMsgs = dc.getMsgs();

        Collections.sort(msgs, new MsgComparator());
        Collections.sort(handleMsgs, new MsgComparator());
        for (int i = 0; i < msgs.size(); i++) {
            assertThat(msgs.get(i), equalTo(handleMsgs.get(i)));
        }
    }

    @Test
    public void testCreate3() throws Exception {
        DebugConsumer dc = DebugConsumer.create();
        MsgExecutor me = MsgExecutor.create(dc, 4);
        List<Msg> msgs = new ArrayList<Msg>();
        for (int i = 0; i < 1000; i++) {
            msgs.add(Msg.create().put("key" + i, "value" + i));
        }
        for (int i = 0; i < 1000; i++) {
            me.handle(msgs.get(i));
            if (i == 500) {
                dc.setSucc(false);
                Thread.sleep(1000);
                dc.setSucc(true);
            }
        }
        while (dc.size() != msgs.size());
        List<Msg> handleMsgs = dc.getMsgs();

        Collections.sort(msgs, new MsgComparator());
        Collections.sort(handleMsgs, new MsgComparator());
        for (int i = 0; i < msgs.size(); i++) {
            assertThat(msgs.get(i), equalTo(handleMsgs.get(i)));
        }
    }

    @Test
    public void testCreate33() throws Exception {
        DebugConsumer dc = DebugConsumer.create();
        MsgExecutor me = MsgExecutor.create(dc, 4);
        List<Msg> msgs = new ArrayList<Msg>();
        for (int i = 0; i < 1000; i++) {
            msgs.add(Msg.create().put("key" + i, "value" + i));
        }
        for (int i = 0; i < 1000; i++) {
            me.handle(msgs.get(i));
            if (i == 500) {
                dc.setSucc(false);
                Thread.sleep(10000);
                dc.setSucc(true);
            }
        }
        while (dc.size() != msgs.size());
        List<Msg> handleMsgs = dc.getMsgs();

        Collections.sort(msgs, new MsgComparator());
        Collections.sort(handleMsgs, new MsgComparator());
        for (int i = 0; i < msgs.size(); i++) {
            assertThat(msgs.get(i), equalTo(handleMsgs.get(i)));
        }
    }

    @Test
    public void testCreate4() throws Exception {
        DebugConsumer dc = DebugConsumer.create();
        MsgExecutor me = MsgExecutor.create(dc, 3);
        List<Msg> msgs = new ArrayList<Msg>();
        for (int i = 0; i < 1000; i++) {
            msgs.add(Msg.create().put("key" + i, "value" + i));
        }
        for (int i = 0; i < 1000; i++) {
            me.handle(msgs.get(i));
            if (i == 500) {
                dc.setSucc(false);
            }
        }
        Thread.sleep(1000);
        dc.setSucc(true);
        while (dc.size() != msgs.size());
        List<Msg> handleMsgs = dc.getMsgs();

        Collections.sort(msgs, new MsgComparator());
        Collections.sort(handleMsgs, new MsgComparator());
        for (int i = 0; i < msgs.size(); i++) {
            assertThat(msgs.get(i), equalTo(handleMsgs.get(i)));
        }
    }


    class MsgComparator implements Comparator<Msg> {
        @Override
        public int compare(Msg o1, Msg o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

}








