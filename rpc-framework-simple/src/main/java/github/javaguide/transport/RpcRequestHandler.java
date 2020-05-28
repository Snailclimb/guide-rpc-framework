package github.javaguide.transport;

import github.javaguide.dto.RpcRequest;
import github.javaguide.dto.RpcResponse;
import github.javaguide.enumeration.RpcResponseCode;
import github.javaguide.registry.DefaultServiceRegistry;
import github.javaguide.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author shuang.kou
 * @createTime 2020年05月13日 09:05:00
 */
public class RpcRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private static final ServiceRegistry serviceRegistry;

    static {
        serviceRegistry = new DefaultServiceRegistry();
    }

    /**
     * 处理 rpcRequest 然后返回方法执行结果
     */
    public Object handle(RpcRequest rpcRequest) {
        Object result = null;
        //通过注册中心获取到目标类（客户端需要调用类）
        Object service = serviceRegistry.getService(rpcRequest.getInterfaceName());
        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("service:{} successful invoke method:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("occur exception", e);
        }
        return result;
    }

    /**
     * 根据 rpcRequest 和 service 对象特定的方法并返回结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        if (null == method) {
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}
