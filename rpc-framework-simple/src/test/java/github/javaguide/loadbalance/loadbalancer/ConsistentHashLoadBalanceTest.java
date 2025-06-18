package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.DemoRpcService;
import github.javaguide.DemoRpcServiceImpl;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.loadbalance.LoadBalance;
import github.javaguide.registry.zk.util.CuratorUtils;
import github.javaguide.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 估计一下QPS
 * 100个任务，每一个线程访问了2001次的zk + 进行了2000次的负载均衡，服务器的列表的大小为100个 执行时间为10s
 *
 * 100 * 1000 = 1e5，总时间为10s
 *
 *  也就是说通信上的时间QPS为10000条数据。如果有1000台这样的服务器，就能在1s内完成1e7也就是千万级别的流量了
 *
 *
 * */
@Slf4j
class ConsistentHashLoadBalanceTest {

    @Test
    void TestZk() {
        DemoRpcService demoRpcService = new DemoRpcServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(demoRpcService).build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(demoRpcService.getClass().getTypeParameters())
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();

        registry(1);
        List<String> address = getAddrs(rpcRequest);
        log.info("地址为:{}", address);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        removeInstance(1);
    }

    @Test
    void TestConsistentHashLoadBalance() {

        // 首先注册100个地址
        for (int i = 0; i < 100; i++) {
            registry(i);
        }


        // 创建100个线程，获取服务,每个线程请求2000次，中间包含一次重构服务地址

        ExecutorService pool = Executors.newFixedThreadPool(16);
        Long currentTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            pool.submit(new GetServiceTask());
        }
        pool.shutdown();

        try {
            // 等待线程池关闭，最多等待30秒
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // 超时后强制关闭
                // 再次等待未完成任务结束
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("线程池未能完全关闭");
                }
            }
        } catch (InterruptedException e) {
            // 主线程被中断时，强制关闭
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }


//        System.out.println("创建了" + ConsistentHashLoadBalanceNew.count.get() + "个实例");

        // 1. 应该只创建一次对象，而不是创建多次
        // 2. 重构不超过100次，因为有100个任务，最多删除100个，可能有删除失败的
        // 3. 时间复杂度不会特别高
        log.info("执行时间:{}s", (System.currentTimeMillis() - currentTime) / 1000);
        log.info("重构的次数:{}", ConsistentHashLoadBalanceNew.count.get());
        assert ConsistentHashLoadBalanceNew.createCount.get() == 1;
        assert ConsistentHashLoadBalanceNew.count.get() <= 100;



    }

    @Slf4j
    static class GetServiceTask implements Runnable {
        @Override
        public void run() {

            LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalanceNew");


            DemoRpcService demoRpcService = new DemoRpcServiceImpl();
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group("test2").version("version2").service(demoRpcService).build();

            // 模拟1000个请求，统计每个节点的负载
            Map<String, Integer> loadDistribution = new HashMap<>();
            for (int i = 0; i < 1000; i++) {
                RpcRequest rpcRequest = RpcRequest.builder()
                        .parameters(demoRpcService.getClass().getTypeParameters())
                        .interfaceName(rpcServiceConfig.getServiceName())
                        .requestId(UUID.randomUUID().toString())
                        .group(rpcServiceConfig.getGroup())
                        .version(rpcServiceConfig.getVersion())
                        .build();
                List<String> serviceUrlList = getAddrs(rpcRequest);
                String selectedNode = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
                loadDistribution.put(selectedNode, loadDistribution.getOrDefault(selectedNode, 0) + 1);
            }

            // 输出负载分布
            log.info("负载分布统计:");
            for (Map.Entry<String, Integer> entry : loadDistribution.entrySet()) {
                log.info(String.format("%s: %d 次请求 (%.2f%%)",
                        entry.getKey(),
                        entry.getValue(),
                        entry.getValue() * 100.0 / 1000));
            }

            // 模拟服务器故障，移除一个节点
            log.info("\n模拟某一个结点故障...");
            removeInstance(new Random().nextInt() % 100);

            // 重新统计负载分布
            loadDistribution.clear();
            for (int i = 0; i < 1000; i++) {
                RpcRequest rpcRequest = RpcRequest.builder()
                        .parameters(demoRpcService.getClass().getTypeParameters())
                        .interfaceName(rpcServiceConfig.getServiceName())
                        .requestId(UUID.randomUUID().toString())
                        .group(rpcServiceConfig.getGroup())
                        .version(rpcServiceConfig.getVersion())
                        .build();
                List<String> serviceUrlList = getAddrs(rpcRequest);
                String selectedNode = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
                loadDistribution.put(selectedNode, loadDistribution.getOrDefault(selectedNode, 0) + 1);
            }

            // 输出新的负载分布
            log.info("节点变更后的负载分布统计:");
            for (Map.Entry<String, Integer> entry : loadDistribution.entrySet()) {
                log.info(String.format("%s: %d 次请求 (%.2f%%)",
                        entry.getKey(),
                        entry.getValue(),
                        entry.getValue() * 100.0 / 1000));
            }
        }


    }

    private static void registry(int i) {
        DemoRpcService demoRpcService = new DemoRpcServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(demoRpcService).build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(demoRpcService.getClass().getTypeParameters())
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        ;
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, i);
//        System.out.println(inetSocketAddress.toString());
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcRequest.getRpcServiceName() + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createEphemeralNode(zkClient, servicePath);
    }

    private static List<String> getAddrs(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        return CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
    }

    private static void removeInstance(int i) {
        DemoRpcService demoRpcService = new DemoRpcServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(demoRpcService).build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(demoRpcService.getClass().getTypeParameters())
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        // 随机删除一个一个结点
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        ;
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, i);
//        System.out.println(inetSocketAddress);
        log.info("删除结点:{}", inetSocketAddress.toString());
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcRequest.getRpcServiceName() + inetSocketAddress.toString();
        CuratorUtils.deleteEphemeralNode(zkClient, servicePath);
    }
}
