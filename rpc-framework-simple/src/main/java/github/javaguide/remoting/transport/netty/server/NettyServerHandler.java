package github.javaguide.remoting.transport.netty.server;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.handler.RpcRequestHandler;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.utils.concurrent.threadpool.CustomThreadPoolConfig;
import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";
    private final RpcRequestHandler rpcRequestHandler;
    private final ExecutorService threadPool;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        customThreadPoolConfig.setCorePoolSize(6);
        this.threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREAD_NAME_PREFIX, customThreadPoolConfig);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        threadPool.execute(() -> {
            try {
                log.info("server receive msg: [{}] ", msg);
                RpcRequest rpcRequest = (RpcRequest) msg;
                //执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    //返回方法执行结果给客户端
                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                    ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    log.error("not writable now, message dropped");
                }
            } finally {
                //确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
