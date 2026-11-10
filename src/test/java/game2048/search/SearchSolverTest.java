package game2048.search;

import game2048.model.Board;
import game2048.model.Direction;
import game2048.model.MoveResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchSolverTest {

    private final SearchSolver solver = new SearchSolver();

    @Test
    void suggestsMergeWhenAvailable() {
        Board board = new Board();
        board.set(0, 0, 2);
        board.set(0, 1, 2);
        board.set(3, 3, 8);

        Direction suggestion = solver.suggest(board);

        assertNotNull(suggestion);
        Board after = board.copy();
        MoveResult result = after.move(suggestion);
        assertTrue(result.getGained() > 0);
    }

    @Test
    void suggestionIsALegalMove() {
        Board board = new Board();
        board.set(1, 1, 4);
        board.set(1, 2, 8);
        board.set(2, 0, 16);

        Direction suggestion = solver.suggest(board);

        assertNotNull(suggestion);
        Board after = board.copy();
        assertTrue(after.move(suggestion).isMoved());
    }

    @Test
    void returnsNullWhenBoardIsFullAndBlocked() {
        Board board = new Board();
        int[][] cells = {
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 8}
        };
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                board.set(row, col, cells[row][col]);
            }
        }

        assertNull(solver.suggest(board));
    }

    @Test
    void returnsNullForEmptyBoard() {
        assertNull(solver.suggest(new Board()));
    }

    @Test
    void evaluationRewardsEmptyCells() {
        Board crowded = new Board();
        crowded.set(0, 0, 2);
        crowded.set(0, 1, 4);
        crowded.set(0, 2, 8);
        crowded.set(0, 3, 16);

        Board roomy = new Board();
        roomy.set(0, 0, 2);

        assertTrue(SearchSolver.evaluate(roomy) > SearchSolver.evaluate(crowded));
    }

    @Test
    void defaultDepthIsThree() {
        assertEquals(3, new SearchSolver().depth());
        assertEquals(3, SearchSolver.DEFAULT_DEPTH);
    }

    @Test
    void customDepthIsHonoured() {
        assertEquals(4, new SearchSolver(4).depth());
        assertEquals(1, new SearchSolver(0).depth());
    }
}
