package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.loadbalance.LoadBalance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


class ConsistentHashLoadBalanceTest {
    @Test
    void TestConsistentHashLoadBalance() {
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
        List<String> serviceUrlList = new ArrayList<>(Arrays.asList("127.0.0.1:9997", "127.0.0.1:9998", "127.0.0.1:9999"));
        String userRpcServiceName = "github.javaguide.UserServicetest1version1";
        String userServiceAddress = loadBalance.selectServiceAddress(serviceUrlList, userRpcServiceName);
        assertEquals("127.0.0.1:9999",userServiceAddress);
        String schoolRpcServiceName = "github.javaguide.SchoolServicetest1version1";
        String schoolServiceAddress = loadBalance.selectServiceAddress(serviceUrlList, schoolRpcServiceName);
        assertEquals("127.0.0.1:9997",schoolServiceAddress);
    }
}