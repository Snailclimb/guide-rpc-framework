package github.javaguide.loadbalance;

import java.util.List;

/**
 * @author shuang.kou
 * @createTime 2020年06月21日 07:44:00
 */
public interface LoadBalance {
    /**
     * 在已有服务提供地址列表中选择一个
     *
     * @param serviceAddresses 服务地址列表
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceAddresses);
}
