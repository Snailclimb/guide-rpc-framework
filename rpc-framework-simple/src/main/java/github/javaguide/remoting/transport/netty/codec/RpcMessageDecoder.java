package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.netty.codec.enums.MySerializableEnum;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author wangtao .
 * @createTime on 2020/10/2
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // default is 8M
        this(RpcConstants.MAX_FRAME_LENGTH);
    }

    public RpcMessageDecoder(int maxFrameLength) {
    /*
        int maxFrameLength,
        int lengthFieldOffset,  magic code is 4B, and version is 1B, and then FullLength. so value is 5
        int lengthFieldLength,  FullLength is int(4B). so values is 4
        int lengthAdjustment,   FullLength include all data and read 9 bytes before, so the left length is (FullLength-9). so values is -9
        int initialBytesToStrip we will check magic code and version self, so do not strip any bytes. so values is 0
        */
        super(maxFrameLength, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }



    private Object decodeFrame(ByteBuf in)
            throws Exception {
//        读取前4个magic比对一下
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
        int fullLength = in.readInt();
        //消息类型
        byte messageType = in.readByte();
        //读取序列化类型
        byte codecType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setMessageType(messageType);
        rpcMessage.setRequestId(requestId);
        rpcMessage.setCodec(codecType);
        if (messageType == RpcConstants.MSGTYPE_HEARTBEAT_REQUEST) {
            rpcMessage.setData(RpcConstants.PING);
        } else if (messageType == RpcConstants.MSGTYPE_HEARTBEAT_RESPONSE) {
            rpcMessage.setData(RpcConstants.PONG);
        } else {
            int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
            if (bodyLength > 0) {
                byte[] bs = new byte[bodyLength];
                in.readBytes(bs);
                String codecName = MySerializableEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                if (messageType == RpcConstants.MSGTYPE_RESQUEST) {
                    RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                    rpcMessage.setData(tmpValue);
                } else {
                    RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                    rpcMessage.setData(tmpValue);
                }
            }
        }
        return rpcMessage;

    }

}
