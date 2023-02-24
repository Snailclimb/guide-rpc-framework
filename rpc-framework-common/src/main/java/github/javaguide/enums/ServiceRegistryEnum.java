package github.javaguide.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author xiaobiaoxu
 * @Date 2023年02月24日 15:30
 */
@AllArgsConstructor
@Getter
public enum ServiceRegistryEnum {

    ZK("zk");

    private final String name;
}
