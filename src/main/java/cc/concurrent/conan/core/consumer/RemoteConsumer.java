package cc.concurrent.conan.core.consumer;

import cc.concurrent.conan.Msg;
import cc.concurrent.conan.util.logging.InternalLogger;
import cc.concurrent.conan.util.logging.InternalLoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cc.concurrent.conan.util.Preconditions.checkArgument;
import static cc.concurrent.conan.util.Preconditions.checkNotNull;
import static cc.concurrent.conan.util.URLEncoderChar.*;

/**
 * User: yanghe.liang
 * Date: 13-8-27
 * Time: 下午10:27
 */
public class RemoteConsumer implements Consumer {

    private final String prefix;
    private final int connectTimeOut;
    private final int readTimeOut;

    private final static int DEFAULT_CONNECT_TIME_OUT = 2000;
    private final static int DEFAULT_READ_TIME_OUT = 2000;

    private final InternalLogger logger = InternalLoggerFactory.getLogger(RemoteConsumer.class);

    private RemoteConsumer(String host, int port, String path, String parameter, int connectTimeOut, int readTimeOut) {
        checkNotNull(host, "host can't be null");
        checkArgument(port > 0, "port must larger than zero");
        checkNotNull(path, "path can't be null");
        checkNotNull(parameter, "parameter can't be null");
        checkArgument(connectTimeOut > 0, "connectTimeOut must larger than zero");
        checkArgument(readTimeOut > 0, "readTimeOut must larger than zero");

        prefix = port == 80 ?
                new StringBuffer("http://").append(host).append("/").append(path).append("?").append(parameter).append("=").toString() :
                new StringBuffer("http://").append(host).append(":").append(port).append("/")
                        .append(path).append("?").append(parameter).append("=").toString();

        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
    }

    public static RemoteConsumer create(String host, int port, String path, String parameter, int connectTimeOut, int readTimeOut) {
        return new RemoteConsumer(host, port, path, parameter, connectTimeOut, readTimeOut);
    }

    public static RemoteConsumer create(String host, int port, String path, String parameter) {
        return create(host, port, path, parameter, DEFAULT_CONNECT_TIME_OUT, DEFAULT_READ_TIME_OUT);
    }

    @Override
    public boolean handle(List<Msg> msgs) {
        HttpURLConnection connection = null;
        try {
            String url = buildUrl(prefix, msgs);
            if (logger.isDebugEnabled()) {
                logger.debug("%s url=%s", Thread.currentThread().getName(), url);
            }
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(connectTimeOut);
            connection.connect();
            connection.setReadTimeout(readTimeOut);
            return connection.getResponseCode() == 204;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    @Override
    public boolean check() {
        return handle(new ArrayList<Msg>());
    }

    static String buildUrl(String prefix, List<Msg> msgs) {
        StringBuffer sb = new StringBuffer(getTotalSize(prefix, msgs));
        sb.append(prefix);
        sb.append(OPEN_BRACKET);
        for (int i = 0; i < msgs.size(); i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(OPEN_BRACE);
            boolean mark = false;
            Msg msg = msgs.get(i);
            for (Map.Entry<String, String> entry : msg.entrySet()) {
                if (mark) {
                    sb.append(COMMA);
                }
                sb.append(QUOTES).append(entry.getKey()).append(QUOTES).append(COLON)
                        .append(QUOTES).append(entry.getValue()).append(QUOTES);
                mark = true;
            }
            sb.append(CLOSE_BRACE);
        }
        sb.append(CLOSE_BRACKET);
        return sb.toString();
    }

    static int getTotalSize(String prefix, List<Msg> msgs) {
        int totalSize = OPEN_BRACKET.size() + CLOSE_BRACKET.size() + prefix.length();
        if (msgs.size() == 0) {
            return totalSize;
        }
        totalSize += (msgs.size() -1) * COMMA.size();
        for (int i = 0; i < msgs.size(); i++) {
            Msg msg = msgs.get(i);
            totalSize += (msg.size() + OPEN_BRACE.size() + CLOSE_BRACE.size());
            if (msg.size() == 0) {
                continue;
            }
            int count = msg.entrySet().size();
            totalSize += count * (QUOTES.size() * 4 + COLON.size());
            totalSize += (count - 1) * COMMA.size();
        }
        return totalSize;
    }

}
