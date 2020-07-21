import github.javaguide.HelloService;
import github.javaguide.serviceimpl.HelloServiceImpl;
import github.javaguide.entity.RpcServiceProperties;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.ServiceProviderImpl;
import github.javaguide.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Server: Manually register the service
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start();
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test").version("1").build();
        serviceProvider.publishService(helloService, rpcServiceProperties);
    }
}
