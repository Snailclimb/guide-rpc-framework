package github.javaguide.registry;

import github.javaguide.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 实现服务注册
 *
 * @author shuang.kou
 * @createTime 2020年05月31日 10:56:00
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + serviceName + inetSocketAddress.toString();
        CuratorUtils.createPersistentNode(servicePath);
    }
}
