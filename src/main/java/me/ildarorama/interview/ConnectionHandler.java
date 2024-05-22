package me.ildarorama.interview;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.ildarorama.interview.game.GameManager;

@Slf4j
public class ConnectionHandler extends SimpleChannelInboundHandler<String> {
    private final GameManager gameManager;

    public ConnectionHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String answer) throws Exception {
        var state = channelHandlerContext.channel().attr(GameManager.GAME_STATE).get();
        gameManager.processAnswer(state, answer);
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelInactive(channelHandlerContext);
        if (channelHandlerContext.channel().hasAttr(GameManager.GAME_STATE)) {
            var state = channelHandlerContext.channel().attr(GameManager.GAME_STATE).get();
            gameManager.deInitPlayer(state);
        }
        log.info("User disconnected");
    }
}
