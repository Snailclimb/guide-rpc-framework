package github.javaguide.remoting.transport.netty.codec.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangtao .
 * @createTime on 2020/10/2
 */
@AllArgsConstructor
@Getter
public enum  MySerializableEnum {

    KYRO((byte) 0x01, "kyro");


    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (MySerializableEnum c : MySerializableEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
