package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.remoting.constants.RpcConstants;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @ClassName RpcMessageFrameDecoder
 * @Description RPCMessageFrame解析器 不能共享
 * @Author jiangyang1556
 * @Date 2025/9/28 9:36
 * @Version 1.0
 **/
public class RpcMessageFrameDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageFrameDecoder() {
        super(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }
}
