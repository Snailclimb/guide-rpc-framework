package github.javaguide.utils;

/**
 * @Description:
 * @Author: zhanghua
 * @Date: 2022/3/6 12:58 上午
 */
public class StringUtils {

	public static boolean isBlank(String s){
		int strLen ;
		if(s == null || (strLen = s.length()) == 0){
			return true;
		}
		for(int i = 0; i < strLen; ++i) {
			if (!Character.isWhitespace(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotBlank(String s){
		return !isBlank(s);
	}

}
