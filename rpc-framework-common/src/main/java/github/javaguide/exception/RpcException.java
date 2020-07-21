package github.javaguide.exception;

import github.javaguide.enumeration.RpcErrorMessage;

/**
 * @author shuang.kou
 * @createTime 2020年05月12日 16:48:00
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessage rpcErrorMessage, String detail) {
        super(rpcErrorMessage.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessage) {
        super(rpcErrorMessage.getMessage());
    }
}
