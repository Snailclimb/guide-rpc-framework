package github.javaguide.transport.netty.server;

import github.javaguide.dto.RpcRequest;
import github.javaguide.dto.RpcResponse;
import github.javaguide.registry.DefaultServiceRegistry;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.transport.RpcRequestHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 20:44:00
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RpcRequestHandler rpcRequestHandler;
    private static ServiceRegistry serviceRegistry;

    static {
        rpcRequestHandler = new RpcRequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            logger.info(String.format("server receive msg: %s", rpcRequest));
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, service);
            logger.info(String.format("server get result: %s", result.toString()));
            ChannelFuture f = ctx.writeAndFlush(RpcResponse.success(result));
            f.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
