package cc.concurrent.conan.util.logging;

import org.junit.Test;

/**
 * User: yanghe.liang
 * Date: 13-9-1
 * Time: 下午12:45
 */
public class InternalLoggerFactoryTest {

    @Test
    public void testGetLogger() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4jLoggerFactory());
        InternalLogger logger = InternalLoggerFactory.getLogger(InternalLoggerFactoryTest.class);
        logger.debug("%s,abc", "hello");

        InternalLoggerFactory.setDefaultFactory(new NOPLoggerFactory());
    }

}
