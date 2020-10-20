package github.javaguide.loadbalance;

import github.javaguide.enums.LoadBalanceEnum;

/**
 * Extended initialization placeholder class mode - 延长初始化占位类模式
 * @author RicardoZ
 * @CreateTime 2020年10月20日
 */
public class LoadBalanceFactory {
    private static class CompletelyRandomHolder {
        public static final RandomLoadBalance RANDOM_LOAD_BALANCE = new RandomLoadBalance();
    }

    private static class ConsistentHashHolder {
        public static final ConsistentHashLoadBalance CONSISTENT_HASH_LOAD_BALANCE = new ConsistentHashLoadBalance();
    }

    public static LoadBalance getInstance(LoadBalanceEnum mode) {
        LoadBalance loadBalance;

        switch (mode) {
            case CONSISTENT_HASH:
                loadBalance = ConsistentHashHolder.CONSISTENT_HASH_LOAD_BALANCE;
                break;
            default:
                loadBalance = CompletelyRandomHolder.RANDOM_LOAD_BALANCE;
        }

        return loadBalance;
    }
}
