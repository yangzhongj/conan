package cc.concurrent.conan.util.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午10:06
 */
public class Log4jLogger implements InternalLogger {

    private final Logger logger;

    Log4jLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(String.format(format, arguments));
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(String.format(format, arguments));
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(String.format(format, arguments));
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(String.format(format, arguments));
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

}
