package server.netty_server;

import server.ContextStoreService;
import server.UserNameService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringMessageHandler extends SimpleChannelInboundHandler<String> {

    private final UserNameService nameService;
    private final ContextStoreService contextStoreService;
    private String name;

    public StringMessageHandler(UserNameService nameService,
                                ContextStoreService contextStoreService) {
        this.nameService = nameService;
        this.contextStoreService = contextStoreService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected");
        nameService.userConnect();
        contextStoreService.registerContext(ctx);
        name = nameService.getUserName();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        log.debug("received: {}", s);
        for (ChannelHandlerContext context : contextStoreService.getContexts()) {
            context.writeAndFlush(name + ": " + s);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected");
        nameService.userDisconnect();
        contextStoreService.removeContext(ctx);
    }
}