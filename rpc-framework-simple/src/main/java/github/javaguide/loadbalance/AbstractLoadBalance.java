package github.javaguide.loadbalance;

import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.utils.CollectionUtil;

import java.util.List;

/**
 * Abstract class for a load balancing policy
 *
 * @author shuang.kou
 * @createTime 2020年06月21日 07:44:00
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 1. 判空
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        // 2. 如果只有一个的情况
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        // 3. 使用钩子函数，进行选择
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

}
