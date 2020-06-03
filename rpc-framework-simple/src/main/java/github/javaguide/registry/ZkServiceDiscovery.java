package github.javaguide.registry;

import github.javaguide.utils.zk.CuratorHelper;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 实现服务发现
 *
 * @author shuang.kou
 * @createTime 2020年06月01日 15:16:00
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO(shuang.kou):feat: 负载均衡
        // 这里直接去了第一个找到的服务地址
        String serviceAddress = CuratorHelper.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址:{}", serviceAddress);
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
