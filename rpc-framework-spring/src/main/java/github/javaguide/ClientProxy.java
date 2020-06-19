package github.javaguide;

import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.remoting.transport.ClientTransport;
import github.javaguide.remoting.transport.netty.client.NettyClientTransport;

/**
 * @description:
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
