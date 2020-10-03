package github.javaguide.remoting.transport.netty.codec;


import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.transport.netty.codec.enums.MySerializableEnum;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author WangTao
 * @createTime on 2020/10/2
 *
 * 自定义协议解码器
 *
 *  * <pre>
 *  * 0     1     2     3     4        5     6     7     8     9          10       11     12    13    14   15
 *  * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+-----------+-----+-----+-----+
 *  * |   magic   code        |version | Full length         | messageType| codec| RequestId                   |
 *  * +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  * |                                                                                                       |
 *  * |                                         body                                                          |
 *  * |                                                                                                       |
 *  * |                                        ... ...                                                        |
 *  * +-------------------------------------------------------------------------------------------------------+
 *
 *  自定义编码器
 *  4B  magic   code 魔法数  1B version 版本  4B full length  消息长度  1B messageType 消息类型
 *   1B codec 序列化   4B  requestId 请求的Id
 *   body object类型数据
 *
 */

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);



    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out)
            throws Exception {
        try {
            int fullLength = RpcConstants.HEAD_LENGTH;
            byte messageType = rpcMessage.getMessageType();
            //写入magic数字
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 留出位置写入数据包的长度
            out.writerIndex(out.writerIndex() + 4);
            //设置消息类型
            out.writeByte(rpcMessage.getMessageType());
            //设置序列化
            out.writeByte(rpcMessage.getCodec());
            out.writeInt(ATOMIC_INTEGER.getAndDecrement());
            byte[] bodyBytes = null;

            //不是心跳
            if (messageType != RpcConstants.MSGTYPE_HEARTBEAT_REQUEST
                    && messageType != RpcConstants.MSGTYPE_HEARTBEAT_RESPONSE) {
                //对象序列化
                String codecName = MySerializableEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }

            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            //写入长度
            out.writeInt(fullLength);
            //重置
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }


    }



}

