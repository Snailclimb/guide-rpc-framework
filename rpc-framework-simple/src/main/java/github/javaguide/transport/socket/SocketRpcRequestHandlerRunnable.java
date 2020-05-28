package github.javaguide.transport.socket;

import github.javaguide.dto.RpcRequest;
import github.javaguide.dto.RpcResponse;
import github.javaguide.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 09:18:00
 */
public class SocketRpcRequestHandlerRunnable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);
    private Socket socket;
    private static final RpcRequestHandler rpcRequestHandler;

    static {
        rpcRequestHandler = new RpcRequestHandler();
    }

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        logger.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }
    }

}
