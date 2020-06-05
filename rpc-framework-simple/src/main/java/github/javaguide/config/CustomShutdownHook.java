package github.javaguide.config;

import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import github.javaguide.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 当服务端（provider）关闭的时候做一些事情比如取消注册所有服务
 *
 * @author shuang.kou
 * @createTime 2020年06月04日 13:11:00
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
