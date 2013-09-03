package cc.concurrent.conan.util.logging;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 上午10:48
 */
public class NOPLoggerFactory extends InternalLoggerFactory {

    @Override
    protected InternalLogger newInstance(String name) {
        return new NOPLogger();
    }

}
