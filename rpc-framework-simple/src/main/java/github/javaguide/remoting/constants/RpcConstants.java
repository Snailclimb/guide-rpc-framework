package github.javaguide.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author wangtao .
 * @createTime on 2020/10/2
 */

public class RpcConstants {


    /**
     * 魔法数 检验 RpcMessage
     * guide rpc
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};


    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    //版本信息
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 15;

    //请求
    public static final byte MSGTYPE_RESQUEST = 1;

    //相应
    public static final byte MSGTYPE_RESPONSE = 2;

    //ping
    public static final byte MSGTYPE_HEARTBEAT_REQUEST = 3;

    //pong
    public static final byte MSGTYPE_HEARTBEAT_RESPONSE = 4;


    public static final int HEAD_LENGTH = 15;


    public static final String PING = "ping";

    public static final String PONG = "pong";


    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
