package cc.concurrent.conan;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * User: yanghe.liang
 * Date: 13-8-29
 * Time: 下午2:27
 */
public class MsgTest {

    @Test
    public void testSize() throws Exception {
        Msg msg = Msg.create();
        assertThat(msg.size(), equalTo(0));
        assertThat(msg.isEmpty(), equalTo(true));

        String key = "key";
        String value = "value";
        int size = key.length() + value.length();
        msg.put(key, value);
        assertThat(msg.size(), equalTo(size));
        assertThat(msg.isEmpty(), equalTo(false));

        key = "key2";
        value = "value2";
        msg.put(key, value);
        size = size + key.length() + value.length();
        assertThat(msg.size(), equalTo(size));

        value = "value22";
        msg.put(key, value);
        size = size + 1;
        assertThat(msg.size(), equalTo(size));

        value = "value";
        msg.put(key, value);
        size = size - 2;
        assertThat(msg.size(), equalTo(size));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut() {
        Msg msg = Msg.create();
        msg.put("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut2() {
        Msg msg = Msg.create();
        msg.put("key", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut3() {
        Msg msg = Msg.create();
        msg.put("key", " ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut4() {
        Msg msg = Msg.create();
        msg.put("+key", "sdf");
    }

    @Test
    public void testPut5() {
        Msg msg = Msg.create();
        msg.put("key", " _ ,.: - ");
    }

}
