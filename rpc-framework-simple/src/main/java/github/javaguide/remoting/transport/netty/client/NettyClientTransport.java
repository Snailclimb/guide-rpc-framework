package github.javaguide.remoting.transport.netty.client;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.ClientTransport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * transport rpcRequest based on netty.
 *
 * @author shuang.kou
 * @createTime 2020年05月29日 11:34:00
 */
@Slf4j
public class NettyClientTransport implements ClientTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;

    public NettyClientTransport() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @Override
    public CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        // get  server address related channel
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // put unprocessed request
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }

}


