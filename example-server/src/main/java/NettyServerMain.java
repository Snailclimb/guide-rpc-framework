import github.javaguide.Hello;
import github.javaguide.HelloService;
import github.javaguide.annotation.RpcScan;
import github.javaguide.remoting.transport.netty.server.NettyRpcServer;
import github.javaguide.serviceimpl.HelloServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Server: Automatic registration service via @RpcService annotation
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
@RpcScan(basePackage = {"github.javaguide"})
public class NettyServerMain {
    public static void main(String[] args) {
        autoRegistry();
    }

    public static void autoRegistry() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        HelloService helloService = applicationContext.getBean(HelloServiceImpl.class);
        helloService.hello(new Hello("你好fzk", "你好服务端"));
        nettyRpcServer.start();
    }
}
