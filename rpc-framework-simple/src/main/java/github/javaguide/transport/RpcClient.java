package github.javaguide.transport;

import github.javaguide.dto.RpcRequest;

/**
 * @author shuang.kou
 * @createTime 2020年05月25日 17:02:00
 */
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
