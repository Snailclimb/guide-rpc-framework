package github.javaguide.transport.socket;

import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.ServiceProviderImpl;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.registry.ZkServiceRegistry;
import github.javaguide.utils.concurrent.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 08:01:00
 */
public class SocketRpcServer {

    private final ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);
    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;


    public SocketRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-server-rpc-pool");
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    private void start() {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
            logger.info("server starts...");
            Socket socket;
            while ((socket = server.accept()) != null) {
                logger.info("client connected");
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("occur IOException:", e);
        }
    }

}
