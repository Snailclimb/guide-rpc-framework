import github.javaguide.HelloService;
import github.javaguide.entity.RpcServiceProperties;
import github.javaguide.remoting.transport.netty.server.NettyServer;
import github.javaguide.serviceimpl.HelloServiceImpl2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Server: Automatic registration service via @RpcService annotation
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
@ComponentScan("github.javaguide")
public class NettyServerMain {
    public static void main(String[] args) {
        // Register service via annotation
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        // Register service manually
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        nettyServer.registerService(helloService2, rpcServiceProperties);
        nettyServer.start();
    }
}
