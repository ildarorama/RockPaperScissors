package me.ildarorama.interview.game;

import io.netty.channel.socket.SocketChannel;
import lombok.Data;

@Data
public class PlayerState {
    private final SocketChannel socketChannel;
    private String nickname;
    private String answer;
    private PlayerState playmate;
    private PlayerState lock;
    private GamePhase phase = GamePhase.WAIT_NICKNAME;

    public PlayerState(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void write(String s) {
        socketChannel.writeAndFlush(s);
    }

    public void close() {
        socketChannel.close();
    }

    public void next() {
        phase = phase.next();
    }

    public void say() {
        switch (phase) {
            case WAIT_NICKNAME -> write("Please input your nickname: ");
            case WAIT_TURN -> write("Give your answer (rock - r, scissors - s, paper - p): ");
            case WAIT_PLAYMATE_TURN -> write("Waiting for playmate tern....\n");
            case WAIT_FOR_PLAYMATE -> write("Waiting for playmate....\n");
            case STATE_LOSER -> write("You lose! Hit enter to close\n");
            case STATE_WINNER -> write("You win! Hit enter to close\n");
        }
    }
}
