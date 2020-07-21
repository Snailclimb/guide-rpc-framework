package github.javaguide.provider;

import github.javaguide.enumeration.RpcErrorMessage;
import github.javaguide.exception.RpcException;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.registry.zk.ZkServiceRegistry;
import github.javaguide.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现了 ServiceProvider 接口，可以将其看做是一个保存和提供服务实例对象的示例
 *
 * @author shuang.kou
 * @createTime 2020年05月13日 11:23:00
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * 接口名和服务的对应关系
     * note:处理一个接口被两个实现类实现的情况如何处理？（通过 group 分组）
     * key:service/interface name
     * value:service
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_SERVICE = ConcurrentHashMap.newKeySet();
    private final ServiceRegistry serviceRegistry = new ZkServiceRegistry();
    /**
     * note:可以修改为扫描注解注册
     */
    @Override
    public void addServiceProvider(Object service, Class<?> serviceClass) {
        String serviceName = serviceClass.getCanonicalName();
        if (REGISTERED_SERVICE.contains(serviceName)) {
            return;
        }
        REGISTERED_SERVICE.add(serviceName);
        SERVICE_MAP.put(serviceName, service);
        log.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = SERVICE_MAP.get(serviceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    public void publishService(Object service) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> anInterface = service.getClass().getInterfaces()[0];
            this.addServiceProvider(service, anInterface);
            serviceRegistry.registerService(anInterface.getCanonicalName(), new InetSocketAddress(host, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
