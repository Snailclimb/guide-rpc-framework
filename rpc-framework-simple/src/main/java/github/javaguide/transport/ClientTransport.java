package github.javaguide.transport;

import github.javaguide.dto.RpcRequest;

/**
 * 传输 RpcRequest。
 *
 * @author shuang.kou
 * @createTime 2020年05月29日 13:26:00
 */
public interface ClientTransport {
    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
