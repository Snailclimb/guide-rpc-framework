package github.javaguide;

import github.javaguide.transport.ClientTransport;
import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.transport.netty.client.NettyClientClientTransport;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "Hello description is 222".equals(hello);
        String hello2 = helloService.hello(new Hello("111", "222"));
        System.out.println(hello2);
    }
}
