package me.ildarorama.interview.game;

public enum GamePhase {
    WAIT_NICKNAME {
        @Override
        public GamePhase next() {
            return WAIT_FOR_PLAYMATE;
        }
    },
    WAIT_FOR_PLAYMATE {
        @Override
        public GamePhase next() {
            return WAIT_TURN;
        }
    },
    WAIT_TURN {
        @Override
        public GamePhase next() {
            return WAIT_PLAYMATE_TURN;
        }
    },
    WAIT_PLAYMATE_TURN {
        @Override
        public GamePhase next() {
            return null;
        }
    },
    STATE_WINNER {
        @Override
        public GamePhase next() {
            return null;
        }
    },
    STATE_LOSER {
        @Override
        public GamePhase next() {
            return null;
        }
    };

    public abstract GamePhase next();
}
