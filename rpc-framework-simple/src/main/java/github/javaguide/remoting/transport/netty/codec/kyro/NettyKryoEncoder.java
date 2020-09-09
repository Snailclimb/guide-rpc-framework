package github.javaguide.remoting.transport.netty.codec.kyro;

import github.javaguide.remoting.transport.netty.codec.enums.SerializableEnum;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 協議格式
 *
 *    | length | serializable id | body length | body data |
 *    |    1   |        2        |       3     |      4    |
 *
 *    1、大端4字節整數，等于2、3、4长度总和
 *    2、大端4字節整數，序列化方法的序號
 *    3、body 長度  大端4字節整數，具體為4的長度
 *    4、body 內容
 * 自定义编码器。负责处理"出站"消息，将消息格式转换字节数组然后写入到字节数据的容器 ByteBuf 对象中。
 * <p>
 * 网络传输需要通过字节流来实现，ByteBuf 可以看作是 Netty 提供的字节数据的容器，使用它会让我们更加方便地处理字节数据。
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 19:43:00
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass;

    private static final int SERIALIZABLE_LENGTH = 4;
    private static final int BODY_LENGTH = 4;


    /**
     * 将对象進行序列化然后写入到 ByteBuf 对象中
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        if (genericClass.isInstance(o)) {
            // 1. 将对象转换为byte
            byte[] body = serializer.serialize(o);
            // 2. 读取消息的长度
            int totalLength = body.length + SERIALIZABLE_LENGTH + BODY_LENGTH;
            // 3.写入消息对应的協議總長度长度,writerIndex 加 4
            byteBuf.writeInt(totalLength);
            // 4.寫入序列化方式的ID,writerIndex 加 4
            byteBuf.writeInt(SerializableEnum.KYRO.getSerializableId());
            // 5.写入消息对应的字节数组长度,writerIndex 加 4
            byteBuf.writeInt(body.length);
            // 6.将字节数组写入 ByteBuf 对象中
            byteBuf.writeBytes(body);
        }
    }

}
