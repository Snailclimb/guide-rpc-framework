package github.javaguide.registry;

import github.javaguide.utils.zk.CuratorHelper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 实现服务发现
 *
 * @author shuang.kou
 * @createTime 2020年06月01日 15:16:00
 */
public class ZkServiceDiscovery implements ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);
    private final CuratorFramework zkClient;

    public ZkServiceDiscovery() {
        this.zkClient = CuratorHelper.getZkClient();
        zkClient.start();
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO 负载均衡
        // 这里直接去了第一个找到的服务地址
        String serviceAddress = CuratorHelper.getChildrenNodes(zkClient, serviceName).get(0);
        logger.info("成功找到服务地址:{}", serviceAddress);
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
