package cc.concurrent.conan.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * User: yanghe.liang
 * Date: 13-8-20
 * Time: 下午6:05
 */
public enum URLEncoderChar {

    OPEN_BRACE("{"),
    CLOSE_BRACE("}"),
    OPEN_BRACKET("["),
    CLOSE_BRACKET("]"),
    COMMA(","),
    QUOTES("\""),
    COLON(":"),
    BLANK(" "),
    ;

    private String s;
    private int size;
    URLEncoderChar(String s) {
        try {
            this.s = URLEncoder.encode(s, "utf8");
            this.size = this.s.length();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return s;
    }

    public int size() {
        return size;
    }

}
