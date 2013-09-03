package cc.concurrent.conan.util.logging;

import org.apache.log4j.Logger;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午10:41
 */
public class Log4jLoggerFactory extends InternalLoggerFactory {

    @Override
    protected InternalLogger newInstance(String name) {
        return new Log4jLogger(Logger.getLogger(name));
    }

}
