import github.javaguide.HelloService;
import github.javaguide.entity.RpcServiceProperties;
import github.javaguide.remoting.transport.socket.SocketRpcServer;
import github.javaguide.serviceimpl.HelloServiceImpl;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        socketRpcServer.registerService(helloService, rpcServiceProperties);
        socketRpcServer.start();
    }
}
