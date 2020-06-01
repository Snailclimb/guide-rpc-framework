package github.javaguide.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author shuang.kou
 * @createTime 2020年06月01日 15:16:00
 */
public interface ServiceDiscovery {
    /**
     * 查找服务
     *
     * @param serviceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress lookupService(String serviceName);
}
