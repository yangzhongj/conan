package cc.concurrent.conan.util.logging;

import org.slf4j.LoggerFactory;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午10:38
 */
public class Slf4jLoggerFactory extends InternalLoggerFactory {

    @Override
    protected InternalLogger newInstance(String name) {
        return new Slf4jLogger(LoggerFactory.getLogger(name));
    }

}
