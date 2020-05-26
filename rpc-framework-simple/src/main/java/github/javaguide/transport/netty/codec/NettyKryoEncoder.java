package github.javaguide.transport.netty.codec;

import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 自定义编码器。负责处理"出站"消息，将消息格式转换字节数组然后写入到字节数据的容日 ByteBuf 对象中。
 * <p>
 * 网络传输需要通过字节流来实现，ByteBuf 可以看作是 Netty 提供的字节数据的容器，使用它会让我们更加方便地处理字节数据。
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 19:43:00
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private Serializer serializer;
    private Class<?> genericClass;

    /**
     * 将对象转换为字节码然后写入到 ByteBuf 对象中
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        if (genericClass.isInstance(o)) {
            // 1. 将对象转换为byte
            byte[] body = serializer.serialize(o);
            // 2. 读取消息的长度
            int dataLength = body.length;
            // 3.写入消息对应的字节数组长度,writerIndex 加 4
            byteBuf.writeInt(dataLength);
            //4.将字节数组写入 ByteBuf 对象中
            byteBuf.writeBytes(body);
        }
    }
}
