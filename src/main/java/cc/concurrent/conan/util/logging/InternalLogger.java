package cc.concurrent.conan.util.logging;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午9:38
 */
public interface InternalLogger {

    public boolean isDebugEnabled();
    public void debug(String msg);
    public void debug(String format, Object... arguments);

    public boolean isInfoEnabled();
    public void info(String msg);
    public void info(String format, Object... arguments);

    public boolean isWarnEnabled();
    public void warn(String msg);
    public void warn(String format, Object... arguments);

    public boolean isErrorEnabled();
    public void error(String msg);
    public void error(String format, Object... arguments);
    public void error(String msg, Throwable t);

}
