package server.netty_server;

import server.ContextStoreService;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import server.autorization.AuthorizationService;

public class HandlerProvider {

    private final ContextStoreService contextStoreService;

    public HandlerProvider(ContextStoreService contextStoreService) {
        this.contextStoreService = contextStoreService;
    }

    public ChannelHandler[] getSerializePipeline(AuthorizationService authService) {
        return new ChannelHandler[] {
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AbstractMessageHandler(authService,contextStoreService)
        };
    }

}