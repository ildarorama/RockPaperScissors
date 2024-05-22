package me.ildarorama.interview;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import me.ildarorama.interview.game.GameManager;

import java.net.InetAddress;

public class StartServer {
    private static final int SERVER_PORT = 2323;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroups = new NioEventLoopGroup();
    private final ServerBootstrap serverBootstrap;
    private final GameManager gameManager = new GameManager();

    private StartServer() {
        serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroups)
                .option(ChannelOption.SO_BACKLOG, 100)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ServerInitHandler(gameManager));
    }

    private void start() throws InterruptedException {
        try {
            serverBootstrap
                    .bind(InetAddress.getLoopbackAddress(), SERVER_PORT)
                    .channel()
                    .closeFuture()
                    .sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroups.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        StartServer server = new StartServer();
        server.start();
    }
}
