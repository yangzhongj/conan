package cc.concurrent.conan.util;

import cc.concurrent.conan.annotation.Nullable;

/**
 * User: yanghe.liang
 * Date: 13-8-29
 * Time: 下午3:03
 */
public class Strings {

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0; // string.isEmpty() in Java 6
    }

}
