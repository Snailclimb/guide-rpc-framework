package github.javaguide;

import github.javaguide.transport.socket.SocketRpcServer;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 8080);
        socketRpcServer.publishService(helloService,HelloService.class);
    }
}
