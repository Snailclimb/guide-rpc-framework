package github.javaguide;

import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.remoting.transport.ClientTransport;
import github.javaguide.remoting.transport.netty.client.NettyClientTransport;

/**
 * @description: 简单写一个方法获取代理对象，其实应该在simple框架里面提供一个接口获取代理对象
 * @author:lvxuhong
 * @date:2020/6/18
 */
public class ClientProxy {

    public static <T> T getServiceProxy(Class<T> serviceClass) {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        return rpcClientProxy.getProxy(serviceClass);
    }
}
