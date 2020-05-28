package github.javaguide;

import github.javaguide.transport.RpcClient;
import github.javaguide.transport.RpcClientProxy;
import github.javaguide.transport.netty.client.NettyRpcClient;
import github.javaguide.transport.socket.SocketRpcClient;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        RpcClient rpcClient = new SocketRpcClient("127.0.0.1", 9999);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
