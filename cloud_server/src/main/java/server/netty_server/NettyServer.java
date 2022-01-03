package server.netty_server;

import server.ContextStoreService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import server.autorization.AuthorizationService;
import server.autorization.DataBaseAuthService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class NettyServer {

    private final AuthorizationService authService;
    private Connection dataBaseConnection;
    private final String name = "Alex";
    private final String pass = "1111";
    private final String connectionURL = "jdbc:mysql://localhost:3306/network_cloud?useUnicode=true&serverTimezone=UTC";

    public NettyServer(){
        try {
            dataBaseConnection = DriverManager.getConnection(connectionURL, name, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        authService = new DataBaseAuthService(dataBaseConnection);
    }

    public void start () {
        HandlerProvider provider = new HandlerProvider(new ContextStoreService());
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    provider.getSerializePipeline(authService)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            log.debug("Server started...");
            future.channel().closeFuture().sync(); // block
        } catch (Exception e) {
            log.error("e=", e);
        } finally {
            authService.stop();
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}