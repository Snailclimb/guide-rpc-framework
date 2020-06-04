package github.javaguide.remoting.transport.netty.client;

import github.javaguide.factory.SingletonFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于获取 Channel 对象
 *
 * @author shuang.kou
 * @createTime 2020年05月29日 16:36:00
 */
@Slf4j
public final class ChannelProvider {

    private static Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static NettyClient nettyClient;

    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    private ChannelProvider() {

    }

    /**
     * 最多重试次数
     */

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 判断是否有对应地址的连接
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            // 如果有的话，判断连接是否可用，可用的话就直接获取
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
        // 否则，重新连接获取 Channel
        Channel channel = nettyClient.doConnect(inetSocketAddress);
        channels.put(key, channel);
        return channel;
    }

}
