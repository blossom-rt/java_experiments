package game2048.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardSlideTest {

    @Test
    void mergesTwoTiles() {
        int[] row = {2, 2, 0, 0};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{4, 0, 0, 0}, row);
        assertEquals(4, result.getGained());
        assertTrue(result.isMoved());
    }

    @Test
    void mergesEachTileOnlyOnce() {
        int[] row = {2, 2, 2, 2};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{4, 4, 0, 0}, row);
        assertEquals(8, result.getGained());
    }

    @Test
    void mergedTileDoesNotMergeAgain() {
        int[] row = {4, 4, 8, 0};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{8, 8, 0, 0}, row);
        assertEquals(8, result.getGained());
    }

    @Test
    void mergesAcrossGap() {
        int[] row = {2, 0, 2, 4};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{4, 4, 0, 0}, row);
        assertEquals(4, result.getGained());
    }

    @Test
    void mergesFourHighTilesIntoTwo() {
        int[] row = {8, 8, 8, 8};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{16, 16, 0, 0}, row);
        assertEquals(32, result.getGained());
    }

    @Test
    void mergeOrderFollowsDirection() {
        int[] row = {2, 2, 4, 4};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{4, 8, 0, 0}, row);
        assertEquals(12, result.getGained());
    }

    @Test
    void trailingTileJoinsFirstPair() {
        int[] row = {4, 4, 4, 0};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{8, 4, 0, 0}, row);
        assertEquals(8, result.getGained());
    }

    @Test
    void reportsNoMoveWhenNothingChanges() {
        int[] row = {2, 4, 2, 4};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{2, 4, 2, 4}, row);
        assertEquals(0, result.getGained());
        assertFalse(result.isMoved());
    }

    @Test
    void movingWithoutMergingCountsAsMove() {
        int[] row = {0, 0, 0, 2};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{2, 0, 0, 0}, row);
        assertEquals(0, result.getGained());
        assertTrue(result.isMoved());
    }

    @Test
    void emptyRowIsNotAMove() {
        int[] row = {0, 0, 0, 0};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{0, 0, 0, 0}, row);
        assertFalse(result.isMoved());
    }

    @Test
    void mergesHighTiles() {
        int[] row = {1024, 1024, 0, 0};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{2048, 0, 0, 0}, row);
        assertEquals(2048, result.getGained());
    }

    @Test
    void fullRowWithoutPairsDoesNotMove() {
        int[] row = {2, 4, 8, 16};
        RowSlide result = Board.slideRow(row);
        assertArrayEquals(new int[]{2, 4, 8, 16}, row);
        assertFalse(result.isMoved());
    }
}
