package github.javaguide;

import github.javaguide.remoting.transport.netty.server.NettyServer;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9998);
        nettyServer.publishService(helloService, HelloService.class);
    }
}
