package github.javaguide.utils;

import java.util.Collection;

/**
 * 集合工具类
 *
 * @author zhanghua
 * @createTime 2022/3/6 12:58 上午
 */
public class CollectionUtil {

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

}
