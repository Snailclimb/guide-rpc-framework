package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.loadbalance.LoadBalance;
import github.javaguide.remoting.dto.RpcRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


class ConsistentHashLoadBalanceTest {
    @Test
    void TestConsistentHashLoadBalance() {
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
        List<String> serviceUrlList = new ArrayList<>(Arrays.asList("127.0.0.1:9997", "127.0.0.1:9998", "127.0.0.1:9999"));
        String userRpcServiceName = "github.javaguide.UserServicetest1version1";
        //build rpcCall
        RpcRequest rpcRequest = RpcRequest.builder()
//                .parameters(args)
                .interfaceName(userRpcServiceName)
//                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group("test2")
                .version("version2")
                .build();
        String userServiceAddress = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        assertEquals("127.0.0.1:9999",userServiceAddress);


        String schoolRpcServiceName = "github.javaguide.SchoolServicetest1version1";
        rpcRequest = RpcRequest.builder()
//                .parameters(args)
                .interfaceName(userRpcServiceName)
//                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group("test2")
                .version("version2")
                .build();
        String schoolServiceAddress = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        assertEquals("127.0.0.1:9997",schoolServiceAddress);
    }
}