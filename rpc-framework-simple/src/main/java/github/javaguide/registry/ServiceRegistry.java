package github.javaguide.registry;

/**
 * 服务注册中心接口
 *
 * @author shuang.kou
 * @createTime 2020年05月13日 08:39:00
 */
public interface ServiceRegistry {
    <T> void register(T service);

    Object getService(String serviceName);
}
