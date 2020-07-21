package github.javaguide.provider;

import github.javaguide.entity.RpcServiceProperties;
import github.javaguide.enumeration.RpcProperties;

/**
 * 保存和提供服务实例对象。服务端使用。
 *
 * @author shuang.kou
 * @createTime 2020年05月31日 16:52:00
 */
public interface ServiceProvider {

    /**
     * @param service              service object
     * @param serviceClass         the interface class implemented by the service instance object
     * @param rpcServiceProperties service related attributes
     */
    void addServiceProvider(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    /**
     * @param rpcServiceProperties service related attributes
     * @return 服务实例对象
     */
    Object getServiceProvider(RpcServiceProperties rpcServiceProperties);

    /**
     * @param service              service object
     * @param rpcServiceProperties service related attributes
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * @param service service object
     */
    void publishService(Object service);
}
