package cc.concurrent.conan.util.logging;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午10:34
 */
public abstract class InternalLoggerFactory {

    private static volatile InternalLoggerFactory defaultFactory = new NOPLoggerFactory();

    public static InternalLoggerFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static void setDefaultFactory(InternalLoggerFactory defaultFactory) {
        if (defaultFactory == null) {
            throw new NullPointerException("defaultFactory");
        }
        InternalLoggerFactory.defaultFactory = defaultFactory;
    }

    public static InternalLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static InternalLogger getLogger(String name) {
        return getDefaultFactory().newInstance(name);
    }

    protected abstract InternalLogger newInstance(String name);

}
