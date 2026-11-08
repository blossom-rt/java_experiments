package game2048.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardMoveTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void movesLeft() {
        board.set(0, 0, 2);
        board.set(0, 1, 2);
        MoveResult result = board.move(Direction.LEFT);
        assertTrue(result.isMoved());
        assertEquals(4, board.get(0, 0));
        assertEquals(0, board.get(0, 1));
    }

    @Test
    void movesRight() {
        board.set(0, 0, 2);
        board.set(0, 1, 2);
        board.move(Direction.RIGHT);
        assertEquals(4, board.get(0, 3));
        assertEquals(0, board.get(0, 2));
        assertEquals(0, board.get(0, 0));
    }

    @Test
    void movesUp() {
        board.set(0, 2, 8);
        board.set(3, 2, 8);
        MoveResult result = board.move(Direction.UP);
        assertTrue(result.isMoved());
        assertEquals(16, board.get(0, 2));
        assertEquals(0, board.get(3, 2));
    }

    @Test
    void movesDown() {
        board.set(0, 1, 4);
        board.set(2, 1, 4);
        board.move(Direction.DOWN);
        assertEquals(8, board.get(3, 1));
        assertEquals(0, board.get(0, 1));
    }

    @Test
    void reportsScoreForMergedTiles() {
        board.set(1, 0, 2);
        board.set(1, 1, 2);
        board.set(2, 0, 4);
        board.set(2, 1, 4);
        MoveResult result = board.move(Direction.LEFT);
        assertEquals(4 + 8, result.getGained());
        assertEquals(0, result.getMoves().get(0).getValue() % 2);
    }

    @Test
    void tracksTileMovementForAnimation() {
        board.set(0, 1, 2);
        MoveResult result = board.move(Direction.LEFT);
        List<TileMove> moves = result.getMoves();
        assertEquals(1, moves.size());
        TileMove move = moves.get(0);
        assertEquals(0, move.getFromRow());
        assertEquals(1, move.getFromCol());
        assertEquals(0, move.getToRow());
        assertEquals(0, move.getToCol());
        assertEquals(2, move.getValue());
        assertFalse(move.isMerged());
    }

    @Test
    void marksMergeInMovement() {
        board.set(0, 0, 2);
        board.set(0, 1, 2);
        List<TileMove> moves = board.move(Direction.LEFT).getMoves();
        assertEquals(2, moves.size());
        assertTrue(moves.get(0).isMerged());
        assertTrue(moves.get(1).isMerged());
        assertEquals(0, moves.get(0).getToCol());
        assertEquals(0, moves.get(1).getToCol());
    }

    @Test
    void unchangedBoardIsNotAMove() {
        board.set(0, 0, 2);
        MoveResult result = board.move(Direction.LEFT);
        assertFalse(result.isMoved());
        assertTrue(result.getMoves().isEmpty());
    }

    @Test
    void canMoveDetectsFullUnmergeableBoard() {
        int[][] cells = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 8}
        };
        fill(cells);
        assertEquals(0, board.emptyCount());
        assertFalse(board.canMove());
    }

    @Test
    void canMoveDetectsAdjacentPair() {
        int[][] cells = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 4}
        };
        fill(cells);
        assertTrue(board.canMove());
    }

    @Test
    void copyIsIndependent() {
        board.set(0, 0, 2);
        Board copy = board.copy();
        copy.set(0, 0, 4);
        assertEquals(2, board.get(0, 0));
    }

    @Test
    void copyFromOverwritesEverything() {
        board.set(0, 0, 2);
        Board other = new Board();
        other.set(3, 3, 8);
        board.copyFrom(other);
        assertEquals(0, board.get(0, 0));
        assertEquals(8, board.get(3, 3));
    }

    private void fill(int[][] cells) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                board.set(row, col, cells[row][col]);
            }
        }
    }
}
