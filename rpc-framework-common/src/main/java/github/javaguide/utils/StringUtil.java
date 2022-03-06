package github.javaguide.utils;

/**
 * String 工具类
 *
 * @author zhanghua
 * @createTime 2022/3/6 12:58 上午
 */
public class StringUtil {

    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
