package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.loadbalance.AbstractLoadBalance;
import github.javaguide.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Zekun Fu
 * @date: 2025/5/11 14:32
 * @Description: 实现能够动态添加和删除结点的一致性hash负载均衡
 *
 * 1. 单例模式创建对象，减少频繁创建对象带来的负载均衡消耗
 * 2. 每次重构服务器列表，采用了无锁（自旋锁） + 双锁检测，减少上下文切换的异常
 * 3. 重构服务器列表前，会对整个列表进行检测，减少无用的重构
 */
@Slf4j
public class ConsistentHashLoadBalanceNew extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashingLoadBalancer> selectors = new ConcurrentHashMap<>();

    // 重构次数，测试使用
    public static AtomicInteger count = new AtomicInteger();

    // 创建次数，测试使用
    public static AtomicInteger createCount = new AtomicInteger();


    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 1. 获取hash选择器
        ConsistentHashingLoadBalancer selector = selectors.get(rpcServiceName);
        if (selector == null) {
            // 2. 如果没有，就新建hash环，使用单例工厂模式进行创建
            selector = SingletonFactory.getInstance(()-> new ConsistentHashingLoadBalancer(
                    serviceAddresses,
                    160,
                    new ConsistentHashingLoadBalancer.MD5HashFunction()), ConsistentHashingLoadBalancer.class);
            selectors.put(rpcServiceName, selector);

        }
        else if (selector.hasChanged(serviceAddresses)) {
            // 3. 如果地址变换了，就重构hash环
            selector = selectors.get(rpcServiceName);
            selector.reBuild(serviceAddresses);
        }
        // 使用请求的uuid进行hash
        return selector.selectNode(rpcServiceName + rpcRequest.getRequestId());
    }


    /**
     *
     * 使用方法：
     * 方式1. 直接创建
     * 方式2. 检测变化，重构hash环
     * */
    static class ConsistentHashingLoadBalancer {

        /**
         * 哈希环定义部分：使用TreeMap存储虚拟节点的哈希值到物理节点的映射
         * 1. 虚拟结点
         * 2. hash函数
         * 3. TreeMap存储结点
         * 4. 物理结点列表
         * */
        private final TreeMap<Long, String> virtualNodes = new TreeMap<>();
        private final Set<String> physicalNodes = new HashSet<>();
        private int virtualNodeCount;
        private HashFunction hashFunction;

        /**
         * 防止使用了没有初始化完成的选择器
         * */
        private volatile boolean initFlag = false;
//        private long identityCode;
        /**
         * 构造函数，在初始化的时候，就需要进行hash环的构建了
         * */
        public ConsistentHashingLoadBalancer(List<String> invokers,
                                             int virtualNodeCount,
                                             HashFunction hashFunction) {
//            count.getAndIncrement();
            log.info("创建服务的选择器");
            this.initFlag = false;
            this.virtualNodeCount = virtualNodeCount;
            this.hashFunction = hashFunction;
            // 1. 构建hash环
            for (String addr : invokers) {
                this.addNode(addr);
            }
//            this.identityCode = this.physicalNodes.hashCode();
            // 2. 初始化完成，可以使用了
            this.initFlag = true;
            createCount.getAndIncrement();
        }

        /**
         * 判断地址列表是否已经发生了变化，不用加上锁
         * */
        public boolean hasChanged(List<String> address) {
            if (address.size() != this.physicalNodes.size()) {
                return true;
            }
            for (String addr: address) {
                if (!this.physicalNodes.contains(addr)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 根据请求的key选择节点
         */
        public String selectNode(String key) {
            while (!initFlag) {
                // 没有初始化完成，直接死循环等待就行了，不要上下文切换，浪费时间
            }
            if (virtualNodes.isEmpty()) {
                return null;
            }

            long keyHash = hashFunction.hash(key);

            // 顺时针找到第一个大于等于keyHash的虚拟节点，获取大于等于keyHash的键值对
            SortedMap<Long, String> tailMap = virtualNodes.tailMap(keyHash);
            Long nodeHash = tailMap.isEmpty() ? virtualNodes.firstKey() : tailMap.firstKey();

            return virtualNodes.get(nodeHash);
        }



        public synchronized void reBuild(List<String> address) {
            // 0.1 重新初始化，防止其他线程获取
            this.initFlag = false;
            // 0.2 首先重新计算一遍，当前的结点是否已经重构了，如果没有线程重构，在进行重构。双检测锁
            if (!this.hasChanged(address)) {
                this.initFlag = true;
                return ;
            }

            log.info("重构服务的选择器");
            count.getAndIncrement();
            // 1. 重构hash环
            // 1.1 获取之前的地址
            Set<String> currentAddress = new HashSet<>(address);
            Set<String> preAddress = new HashSet<>(this.physicalNodes);
            // 1.2. 找到需要删除和需要新增的
            List<String> readyToRemove = new ArrayList<>();
            List<String> readyToAdd = new ArrayList<>();
            for (String addr : address) {
                if (!preAddress.contains(addr)) {
                    readyToAdd.add(addr);
                }
            }
            for (String addr: this.physicalNodes) {
                if (!currentAddress.contains(addr)) {
                    readyToRemove.add(addr);
                }
            }
            // 1.3. 重构hash环
            for (String r: readyToRemove) {
                this.removeNode(r);
            }
            for (String a : readyToAdd) {
                this.addNode(a);
            }

            // 2. 变量赋值
            this.initFlag = true;
//            this.identityCode = this.physicalNodes.hashCode();
            log.info("重新构建的列表大小:{}", this.physicalNodes.size());
        }

        /**
         * 添加物理节点
         */
        private void addNode(String node) {
            if (physicalNodes.contains(node)) {
                return;
            }
            physicalNodes.add(node);

            // 为每个物理节点创建虚拟节点
            for (int i = 0; i < virtualNodeCount; i++) {
                String virtualNodeName = node + "#" + i;
                long hash = hashFunction.hash(virtualNodeName);
                virtualNodes.put(hash, node);
            }
        }

        /**
         * 移除物理节点
         */
        private void removeNode(String node) {
            if (!physicalNodes.contains(node)) {
                return;
            }
            physicalNodes.remove(node);

            // 移除该物理节点对应的所有虚拟节点
            for (int i = 0; i < virtualNodeCount; i++) {
                String virtualNodeName = node + "#" + i;
                long hash = hashFunction.hash(virtualNodeName);
                virtualNodes.remove(hash);
            }
        }
        /**
         * 获取所有物理节点
         */
        public List<String> getAllNodes() {
            while (!initFlag) {
                // 获取结点前，首先保证初始化完成了
            }
            return Collections.unmodifiableList(new ArrayList<>(physicalNodes));
        }

        /**
         * 哈希函数接口
         */
        public interface HashFunction {
            long hash(String key);
        }

        /**
         * MD5hash摘要算法
         */
        public static class MD5HashFunction implements HashFunction {
            @Override
            public long hash(String key) {
                try {
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    byte[] digest = md5.digest(key.getBytes());

                    // 取前8字节作为long类型的哈希值
                    return ((long) (digest[0] & 0xFF) << 56) |
                            ((long) (digest[1] & 0xFF) << 48) |
                            ((long) (digest[2] & 0xFF) << 40) |
                            ((long) (digest[3] & 0xFF) << 32) |
                            ((long) (digest[4] & 0xFF) << 24) |
                            ((long) (digest[5] & 0xFF) << 16) |
                            ((long) (digest[6] & 0xFF) << 8) |
                            (digest[7] & 0xFF);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }



}
