package github.javaguide.transport.netty.server;

import github.javaguide.dto.RpcRequest;
import github.javaguide.dto.RpcResponse;
import github.javaguide.registry.DefaultServiceRegistry;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.transport.RpcRequestHandler;
import github.javaguide.utils.concurrent.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.debugger.Page;

import java.util.concurrent.ExecutorService;

/**
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据。
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 20:44:00
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;
    private static ExecutorService threadPool;

    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("netty-server-handler-rpc-pool");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        threadPool.execute(() -> {
            logger.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
            try {
                logger.info(String.format("server receive msg: %s", msg));
                RpcRequest rpcRequest = (RpcRequest) msg;
                String interfaceName = rpcRequest.getInterfaceName();
                //通过注册中心获取到目标类（客户端需要调用类）
                Object service = serviceRegistry.getService(interfaceName);
                //执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest, service);
                logger.info(String.format("server get result: %s", result.toString()));
                //返回方法执行结果给客户端
                ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result));
                f.addListener(ChannelFutureListener.CLOSE);
            } finally {
                //确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
