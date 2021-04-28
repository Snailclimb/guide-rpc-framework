package github.javaguide.registry;

import github.javaguide.registry.zk.ZkServiceDiscovery;
import github.javaguide.registry.zk.ZkServiceRegistry;
import github.javaguide.remoting.dto.RpcRequest;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author shuang.kou
 * @createTime 2020年05月31日 16:25:00
 */
class ZkServiceRegistryTest {

    @Test
    void should_register_service_successful_and_lookup_service_by_service_name() {
        ServiceRegistry zkServiceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        zkServiceRegistry.registerService("github.javaguide.registry.zk.ZkServiceRegistry", givenInetSocketAddress);
        ServiceDiscovery zkServiceDiscovery = new ZkServiceDiscovery();
        RpcRequest rpcRequest = RpcRequest.builder()
//                .parameters(args)
                .interfaceName("github.javaguide.registry.zk.ZkServiceRegistry")
//                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group("test2")
                .version("version2")
                .build();
        InetSocketAddress acquiredInetSocketAddress = zkServiceDiscovery.lookupService(rpcRequest);
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());
    }
}
