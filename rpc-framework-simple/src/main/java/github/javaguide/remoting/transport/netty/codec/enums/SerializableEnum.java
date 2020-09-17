package github.javaguide.remoting.transport.netty.codec.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: tydhot (583125614@qq.com)
 * @date:2020/9/9
 **/
@AllArgsConstructor
@Getter
public enum SerializableEnum {

    KYRO(1, "kyro");

    private Integer serializableId;

    private String serializableKey;

}
