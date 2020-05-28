package github.javaguide.transport;

import github.javaguide.dto.RpcRequest;

/**
 * 实现了 RpcClient 接口的对象需要具有发送 RpcRequest 的能力
 *
 * @author shuang.kou
 * @createTime 2020年05月25日 17:02:00
 */
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
