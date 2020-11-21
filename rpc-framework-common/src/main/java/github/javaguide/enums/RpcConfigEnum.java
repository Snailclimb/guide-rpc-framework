package github.javaguide.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;

}
