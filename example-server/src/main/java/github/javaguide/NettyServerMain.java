package github.javaguide;

import github.javaguide.api.HelloService;
import github.javaguide.remoting.transport.netty.server.NettyServer;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class NettyServerMain {
    public static void main(String[] args) {

        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.publishService(helloService, HelloService.class);
    }
}
