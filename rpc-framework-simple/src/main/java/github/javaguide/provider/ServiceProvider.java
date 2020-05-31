package github.javaguide.provider;

/**
 * 保存和提供服务实例对象。服务端使用。
 *
 * @author shuang.kou
 * @createTime 2020年05月31日 16:52:00
 */
public interface ServiceProvider {
    /**
     * 保存服务提供者
     */
    <T> void addServiceProvider(T service);

    /**
     * 获取服务提供者
     */
    Object getServiceProvider(String serviceName);
}
