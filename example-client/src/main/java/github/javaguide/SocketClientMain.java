package github.javaguide;

import github.javaguide.entity.RpcServiceProperties;
import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.remoting.transport.RpcRequestTransport;
import github.javaguide.remoting.transport.socket.SocketRpcClient;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceProperties);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
