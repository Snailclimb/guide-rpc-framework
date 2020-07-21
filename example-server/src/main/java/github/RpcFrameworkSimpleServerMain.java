package github;

import github.javaguide.HelloService;
import github.javaguide.serviceimpl.HelloServiceImpl;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.ServiceProviderImpl;
import github.javaguide.remoting.transport.socket.SocketRpcServer;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 8080);
        socketRpcServer.start();
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        serviceProvider.publishService(helloService);
    }
}
