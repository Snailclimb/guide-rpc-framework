package github.javaguide.transport.socket;

import github.javaguide.utils.concurrent.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 08:01:00
 */
public class SocketRpcServer {

    private ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(SocketRpcServer.class);

    public SocketRpcServer() {
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-server-rpc-pool");
    }

    public void start(int port) {

        try (ServerSocket server = new ServerSocket(port);) {
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
