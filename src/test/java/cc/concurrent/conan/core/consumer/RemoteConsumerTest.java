package cc.concurrent.conan.core.consumer;

import cc.concurrent.conan.Msg;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * User: yanghe.liang
 * Date: 13-9-2
 * Time: 下午10:20
 */
public class RemoteConsumerTest {

    @Test
    public void testBuildUrl() throws Exception {
        List<Msg> msgs = new ArrayList<Msg>();
        msgs.add(Msg.create().put("key", "._,: -").put("key2", "1234"));
        msgs.add(Msg.create().put("key3", "._,: -").put("key4", "5678"));
        String prefix = "";
        String json = URLDecoder.decode(RemoteConsumer.buildUrl(prefix, msgs), "utf8");

        JsonArray ja = new JsonArray();
        JsonObject jo = new JsonObject();
        jo.addProperty("key", "._,: -");
        jo.addProperty("key2", "1234");
        ja.add(jo);
        jo = new JsonObject();
        jo.addProperty("key3", "._,: -");
        jo.addProperty("key4", "5678");
        ja.add(jo);

        assertThat(ja.toString(), equalTo(json));
    }

    @Test
    public void testGetTotalSize() throws Exception {
        List<Msg> msgs = new ArrayList<Msg>();
        String prefix = "prefix";
        int actual = RemoteConsumer.getTotalSize(prefix, msgs);
        String url = RemoteConsumer.buildUrl(prefix, msgs);
        assertThat(actual, equalTo(url.length()));
    }

    @Test
    public void testGetTotalSize2() throws Exception {
        List<Msg> msgs = new ArrayList<Msg>();
        msgs.add(Msg.create().put("key", "._,: -"));
        String prefix = "prefix";
        int actual = RemoteConsumer.getTotalSize(prefix, msgs);
        String url = RemoteConsumer.buildUrl(prefix, msgs);
        assertThat(actual, equalTo(url.length()));
    }

    @Test
    public void testGetTotalSize3() throws Exception {
        List<Msg> msgs = new ArrayList<Msg>();
        msgs.add(Msg.create().put("key", "._,: -").put("key2", "sd12121"));
        String prefix = "prefix";
        int actual = RemoteConsumer.getTotalSize(prefix, msgs);
        String url = RemoteConsumer.buildUrl(prefix, msgs);
        assertThat(actual, equalTo(url.length()));
    }

    @Test
    public void testGetTotalSize4() throws Exception {
        List<Msg> msgs = new ArrayList<Msg>();
        msgs.add(Msg.create().put("key", "._,: -").put("key2", "sd12121"));
        msgs.add(Msg.create().put("key3", "._,: -").put("key4", "sd12121"));
        String prefix = "prefix";
        int actual = RemoteConsumer.getTotalSize(prefix, msgs);
        String url = RemoteConsumer.buildUrl(prefix, msgs);
        assertThat(actual, equalTo(url.length()));
    }

}
