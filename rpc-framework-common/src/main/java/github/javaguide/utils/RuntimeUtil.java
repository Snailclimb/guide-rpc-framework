package github.javaguide.utils;

/**
 * @author hty
 * @createTime 2020年09月18日 15:43:00
 */
public class RuntimeUtil {
    /**
     * 获取CPU的核心数
     *
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
