package cc.concurrent.conan.util.logging;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午10:49
 */
public class NOPLogger implements  InternalLogger {

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {
    }

    @Override
    public void debug(String format, Object... arguments) {
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void info(String format, Object... arguments) {
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
    }

    @Override
    public void warn(String format, Object... arguments) {
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
    }

    @Override
    public void error(String format, Object... arguments) {
    }

    @Override
    public void error(String msg, Throwable t) {
    }

}
