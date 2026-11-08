package game2048.model;

import game2048.storage.HighScoreStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    @TempDir
    Path tempDir;

    private Game newGame() {
        return new Game(new Random(7), new HighScoreStore(tempDir.resolve("score.txt")), Difficulty.NORMAL);
    }

    @Test
    void restartStartsWithTwoTiles() {
        Game game = newGame();
        assertEquals(2, countTiles(game.board()));
        assertEquals(0, game.score());
        assertEquals(Game.Status.PLAYING, game.status());
    }

    @Test
    void mergeAddsScore() {
        Game game = newGame();
        game.board().clear();
        game.board().set(0, 0, 2);
        game.board().set(0, 1, 2);

        game.move(Direction.LEFT);

        assertEquals(4, game.score());
        assertEquals(4, game.board().get(0, 0));
    }

    @Test
    void undoRestoresBoardAndScore() {
        Game game = newGame();
        game.board().clear();
        game.board().set(0, 0, 2);
        game.board().set(0, 1, 2);
        game.move(Direction.LEFT);
        assertEquals(4, game.score());

        assertTrue(game.undo());

        assertEquals(0, game.score());
        assertEquals(2, game.board().get(0, 0));
        assertEquals(2, game.board().get(0, 1));
        assertEquals(2, countTiles(game.board()));
    }

    @Test
    void undoWithoutHistoryDoesNothing() {
        Game game = newGame();
        assertFalse(game.canUndo());
        assertFalse(game.undo());
    }

    @Test
    void winStopsTheGameAndSkipsSpawn() {
        Game game = newGame();
        game.board().clear();
        game.board().set(0, 0, 1024);
        game.board().set(0, 1, 1024);

        game.move(Direction.LEFT);

        assertTrue(game.hasWon());
        assertEquals(Game.Status.WON, game.status());
        assertEquals(2048, game.score());
        assertEquals(15, game.board().emptyCount());
    }

    @Test
    void continueAfterWinResumesPlay() {
        Game game = newGame();
        game.board().clear();
        game.board().set(0, 0, 1024);
        game.board().set(0, 1, 1024);
        game.move(Direction.LEFT);

        game.continueAfterWin();
        assertEquals(Game.Status.PLAYING, game.status());

        game.move(Direction.DOWN);
        assertEquals(2048, game.board().get(3, 0));
    }

    @Test
    void blockedBoardIsGameOver() {
        Game game = newGame();
        fill(game.board(), new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 8}
        });

        assertTrue(game.isGameOver());
        assertFalse(game.move(Direction.LEFT).isMoved());
        assertEquals(Game.Status.OVER, game.status());
    }

    @Test
    void movesAreIgnoredAfterGameOver() {
        Game game = newGame();
        fill(game.board(), new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 8}
        });
        game.move(Direction.LEFT);

        assertFalse(game.move(Direction.UP).isMoved());
        assertFalse(game.canUndo());
    }

    @Test
    void bestScoreIsPersisted() {
        Path file = tempDir.resolve("best.txt");
        HighScoreStore store = new HighScoreStore(file);
        Game game = new Game(new Random(1), store, Difficulty.NORMAL);
        game.board().clear();
        game.board().set(0, 0, 2);
        game.board().set(0, 1, 2);
        game.move(Direction.LEFT);

        assertEquals(4, game.best());
        assertEquals(4, store.load());

        Game next = new Game(new Random(1), store, Difficulty.NORMAL);
        assertEquals(4, next.best());
    }

    @Test
    void difficultyChangesSpawnOdds() {
        assertEquals(0.10, Difficulty.EASY.fourChance(), 0.0001);
        assertEquals(0.50, Difficulty.HARD.fourChance(), 0.0001);
        assertEquals(Difficulty.NORMAL, Difficulty.EASY.next());
    }

    private static int countTiles(Board board) {
        int count = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.get(row, col) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void fill(Board board, int[][] cells) {
        board.clear();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                board.set(row, col, cells[row][col]);
            }
        }
    }
}
