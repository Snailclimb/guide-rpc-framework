package github.javaguide.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 *
 * @author shuang.kou
 * @createTime 2020年05月13日 08:39:00
 */
public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);

}
