package cc.concurrent.conan;

import cc.concurrent.conan.util.Strings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static cc.concurrent.conan.util.Preconditions.checkArgument;
import static cc.concurrent.conan.util.URLEncoderChar.BLANK;
import static cc.concurrent.conan.util.URLEncoderChar.COLON;
import static cc.concurrent.conan.util.URLEncoderChar.COMMA;


/**
 * User: yanghe.liang
 * Date: 13-7-14
 * Time: 上午8:46
 */
public class Msg {

    private final static String KEY_PATTERN = "[a-zA-Z_][\\w_]*";
    private final static Pattern COMPILE_KEY_PATTERN = Pattern.compile(KEY_PATTERN);
    private final static String VALUE_PATTERN = "[\\w\\._,: -]+";
    private final static Pattern COMPILE_VALUE_PATTERN = Pattern.compile(VALUE_PATTERN);

    // 使用LinkedHashMap会按照put顺序取出数据
    private final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
    private int size = 0;

    private Msg() {
    }

    public static Msg create() {
        return new Msg();
    }

    public Msg put(String key, Object value) {
        put(key, value.toString());
        return this;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return map.entrySet();
    }

    private void put(String key, String value) {
        checkArgument(!Strings.isNullOrEmpty(key), "key can't be null or empty");
        checkArgument(!Strings.isNullOrEmpty(value), "value can't be null or empty");
        value = value.trim();
        checkArgument(value.length() > 0, "value can't be blank");
        checkArgument(COMPILE_KEY_PATTERN.matcher(key).matches(), "key need regex pattern \"%s\" but key is \"%s\"", KEY_PATTERN, key);
        checkArgument(COMPILE_VALUE_PATTERN.matcher(value).matches(), "value need regex pattern \"%s\" but value is \"%s\"", VALUE_PATTERN, value);

        value = value.replaceAll(",", COMMA.toString()).replaceAll(":", COLON.toString()).replaceAll(" ", BLANK.toString());
        String oldValue = map.put(key, value);
        size += (oldValue == null ? key.length() + value.length() : value.length() - oldValue.length());
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
