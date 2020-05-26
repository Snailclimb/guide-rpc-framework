package github.javaguide.transport.netty.client;

import github.javaguide.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义客户端 ChannelHandler 来处理服务端发过来的数据
 *
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 20:50:00
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * 读取服务端传输的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            logger.info(String.format("client receive msg: %s", msg));
            RpcResponse rpcResponse = (RpcResponse) msg;
            // 声明一个 AttributeKey 对象，类似于 Map 中的 key
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            /*
             * AttributeMap 可以看作是一个Channel的共享数据源
             * AttributeMap 的 key 是 AttributeKey，value 是 Attribute
             */
            // 将服务端的返回结果保存到 AttributeMap 上
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理客户端消息发生异常的时候被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

