package github.javaguide.utils;

import java.util.Collection;

/**
 * @Description:
 * @Author: zhanghua
 * @Date: 2022/3/6 12:58 上午
 */
public class CollectionUtils {

	public static boolean isEmpty(Collection coll) {
		return coll == null || coll.isEmpty();
	}

	public static boolean isNotEmpty(Collection coll) {
		return isEmpty(coll);
	}
}
