package me.ildarorama.interview.game;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GameManager {
    public static final AttributeKey<PlayerState> GAME_STATE = AttributeKey.valueOf("gameState");
    private final AtomicReference<PlayerState> lobby = new AtomicReference<>();

    public Optional<PlayerState> getPlaymateOrAddToLobby(PlayerState game) {
        PlayerState expectedValue = lobby.get();
        PlayerState savedValue = expectedValue == null ? game : null;

        while (!lobby.compareAndSet(expectedValue, savedValue)) {
            expectedValue = lobby.get();
            savedValue = expectedValue == null ? game : null;
        }
        return Optional.ofNullable(expectedValue);
    }

    private void processNickName(PlayerState player, String answer) {
        player.setNickname(answer);
        player.next();

        log.info("New nickname {}", player.getNickname());

        var playMateOp = getPlaymateOrAddToLobby(player);
        if (playMateOp.isPresent()) {
            startNewGame(playMateOp.get(), player);
        } else {
            player.say();
        }
    }

    private void startNewGame(PlayerState player1, PlayerState player2) {
        player1.setPlaymate(player2);
        player2.setPlaymate(player1);
        player1.setLock(player1);
        player2.setLock(player1);

        player1.write("Game started. Your playmate is " + player2.getNickname() + "\n");
        player2.write("Game started. Your playmate is " + player1.getNickname() + "\n");

        player1.next();
        player2.next();

        player1.say();
        player2.say();

        log.info("New game {} <-> {}", player1.getNickname(), player2.getNickname());
    }

    public void initPlayer(SocketChannel socketChannel) {
        var playerState = new PlayerState(socketChannel);
        socketChannel
                .attr(GameManager.GAME_STATE)
                .set(playerState);
        playerState.say();
    }

    public void processAnswer(PlayerState state, String answer) {
        switch (state.getPhase()) {
            case WAIT_NICKNAME -> processNickName(state, answer);
            case WAIT_TURN -> processTurn(state, answer);
            case STATE_WINNER, STATE_LOSER -> state.close();
        }
    }

    private void processTurn(PlayerState state, String answer) {
        if (StringUtils.equalsAnyIgnoreCase(answer, "r", "s", "p")) {
            state.setAnswer(answer);
            state.next();

            boolean playmateTurned;
            synchronized (state.getLock()) {
                playmateTurned = state.getPlaymate().getPhase() == GamePhase.WAIT_PLAYMATE_TURN;
                if (playmateTurned) {
                    checkForWinner(state);
                }
            }

            state.say();
            if (playmateTurned) {
                state.getPlaymate().say();
            }
        } else {
            state.say();
        }
    }

    private void checkForWinner(PlayerState state) {
        String s1 = state.getAnswer();
        String s2 = state.getPlaymate().getAnswer();

        if (StringUtils.equalsIgnoreCase(s1, s2)) {
            state.setPhase(GamePhase.WAIT_TURN);
            state.getPlaymate().setPhase(GamePhase.WAIT_TURN);
            return;
        }

        PlayerState winner;
        PlayerState looser;
        if (("r".equalsIgnoreCase(s1) && "s".equalsIgnoreCase(s2)) ||
                ("s".equalsIgnoreCase(s1) && "p".equalsIgnoreCase(s2))) {
            winner = state;
            looser = state.getPlaymate();
        } else {
            winner = state.getPlaymate();
            looser = state;
        }
        winner.setPhase(GamePhase.STATE_WINNER);
        looser.setPhase(GamePhase.STATE_LOSER);
    }

    public void deInitPlayer(PlayerState state) {
        lobby.compareAndSet(state, null);
        if (state.getPlaymate() != null) {
            state.getPlaymate().write("Playmate disconnected\n");
            state.getPlaymate().close();
        }
    }
}
