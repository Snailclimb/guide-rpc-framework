package github.javaguide.remoting.dto;


import lombok.*;

/**
 * @author wangtao
 * @createTime 2020年10月2日 12:33
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    //消息类型
    private byte messageType;

    //序列化类型
    private byte codec;

    //请求id
    private int requestId;

    //数据内容
    private Object data;


}
