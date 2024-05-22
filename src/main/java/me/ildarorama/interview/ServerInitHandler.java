package me.ildarorama.interview;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import me.ildarorama.interview.game.GameManager;

@Slf4j
@ChannelHandler.Sharable
public class ServerInitHandler extends ChannelInitializer<SocketChannel> {
    private final GameManager gameManager;

    public ServerInitHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter()))
                .addLast(new StringDecoder())
                .addLast(new StringEncoder())
                .addLast(new ConnectionHandler(gameManager));
        gameManager.initPlayer(socketChannel);
        log.info("New connection established");
    }
}
