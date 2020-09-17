package github.javaguide.remoting.transport.netty.codec.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author tydhot (583125614@qq.com)
 * @createTime 2020年09月9日
 **/
@AllArgsConstructor
@Getter
public enum SerializableEnum {

    KYRO(1, "kyro");

    private final Integer serializableId;

    private final String serializableKey;

}
