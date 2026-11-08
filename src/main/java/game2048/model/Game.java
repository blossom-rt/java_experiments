package game2048.model;

import game2048.storage.HighScoreStore;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * 一局游戏：棋盘、分数、最高分、难度、撤销历史与胜负状态。
 */
public final class Game {
    public enum Status { PLAYING, WON, OVER }

    public static final int HISTORY_LIMIT = 30;

    private final Board board = new Board();
    private final Random random;
    private final HighScoreStore store;
    private final Deque<Snapshot> history = new ArrayDeque<Snapshot>();

    private Difficulty difficulty;
    private int score;
    private int best;
    private boolean won;
    private Status status = Status.PLAYING;

    public Game(Random random, HighScoreStore store, Difficulty difficulty) {
        this.random = random;
        this.store = store;
        this.difficulty = difficulty;
        this.best = store.load();
        restart();
    }

    public Board board() {
        return board;
    }

    public int score() {
        return score;
    }

    public int best() {
        return best;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Status status() {
        return status;
    }

    public boolean hasWon() {
        return won;
    }

    public boolean isGameOver() {
        return status == Status.OVER || (status != Status.WON && !board.canMove());
    }

    public boolean canUndo() {
        return !history.isEmpty() && status != Status.OVER;
    }

    public void restart() {
        board.clear();
        history.clear();
        score = 0;
        won = false;
        status = Status.PLAYING;
        addRandomTile();
        addRandomTile();
    }

    public MoveResult move(Direction direction) {
        if (status == Status.PLAYING && !board.canMove()) {
            status = Status.OVER;
        }
        if (status != Status.PLAYING) {
            return MoveResult.NONE;
        }
        Snapshot before = new Snapshot(board.copy(), score, won, status);
        MoveResult result = board.move(direction);
        if (!result.isMoved()) {
            return result;
        }

        pushHistory(before);
        score += result.getGained();

        if (!won && board.hasValue(2048)) {
            won = true;
            status = Status.WON;
        } else {
            addRandomTile();
            if (!board.canMove()) {
                status = Status.OVER;
            }
        }
        if (score > best) {
            best = score;
            store.save(best);
        }
        return result;
    }

    public boolean undo() {
        Snapshot snapshot = history.pollLast();
        if (snapshot == null) {
            return false;
        }
        board.copyFrom(snapshot.board);
        score = snapshot.score;
        won = snapshot.won;
        status = snapshot.status;
        return true;
    }

    public void continueAfterWin() {
        if (status != Status.WON) {
            return;
        }
        status = board.canMove() ? Status.PLAYING : Status.OVER;
    }

    private void pushHistory(Snapshot snapshot) {
        history.addLast(snapshot);
        while (history.size() > HISTORY_LIMIT) {
            history.removeFirst();
        }
    }

    private void addRandomTile() {
        int emptyCount = board.emptyCount();
        if (emptyCount == 0) {
            return;
        }
        int target = random.nextInt(emptyCount);
        int seen = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.get(row, col) != 0) {
                    continue;
                }
                if (seen == target) {
                    double roll = random.nextDouble();
                    board.set(row, col, roll < difficulty.fourChance() ? 4 : 2);
                    return;
                }
                seen++;
            }
        }
    }

    private static final class Snapshot {
        private final Board board;
        private final int score;
        private final boolean won;
        private final Status status;

        Snapshot(Board board, int score, boolean won, Status status) {
            this.board = board;
            this.score = score;
            this.won = won;
            this.status = status;
        }
    }
}
