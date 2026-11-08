package game2048.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 棋盘状态与移动规则，不涉及任何界面代码。
 */
public final class Board {
    public static final int SIZE = 4;

    private final int[][] cells = new int[SIZE][SIZE];

    public int get(int row, int col) {
        return cells[row][col];
    }

    public void set(int row, int col, int value) {
        cells[row][col] = value;
    }

    public void clear() {
        for (int row = 0; row < SIZE; row++) {
            Arrays.fill(cells[row], 0);
        }
    }

    public Board copy() {
        Board copy = new Board();
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(cells[row], 0, copy.cells[row], 0, SIZE);
        }
        return copy;
    }

    public void copyFrom(Board other) {
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(other.cells[row], 0, cells[row], 0, SIZE);
        }
    }

    public int emptyCount() {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean hasValue(int value) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (cells[row][col] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canMove() {
        if (emptyCount() > 0) {
            return true;
        }
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int value = cells[row][col];
                if (row + 1 < SIZE && cells[row + 1][col] == value) {
                    return true;
                }
                if (col + 1 < SIZE && cells[row][col + 1] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 纯函数：把一行按"向左"方向压缩、合并、再压缩，直接改写入参数数组。
     */
    public static RowSlide slideRow(int[] row) {
        int[] destination = new int[SIZE];
        int[] partner = new int[SIZE];
        return slideCore(row, destination, partner);
    }

    /** 对整盘执行一次移动，返回是否有效、得分，以及每个方块的位移。 */
    public MoveResult move(Direction direction) {
        List<TileMove> moves = new ArrayList<TileMove>();
        boolean moved = false;
        int gained = 0;

        for (int line = 0; line < SIZE; line++) {
            int[] rowOf = new int[SIZE];
            int[] colOf = new int[SIZE];
            int[] values = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                rowOf[i] = direction.rowAt(line, i);
                colOf[i] = direction.colAt(line, i);
                values[i] = cells[rowOf[i]][colOf[i]];
            }

            int[] original = values.clone();
            int[] destination = new int[SIZE];
            int[] partner = new int[SIZE];
            RowSlide slide = slideCore(values, destination, partner);
            moved |= slide.isMoved();
            gained += slide.getGained();

            for (int i = 0; i < SIZE; i++) {
                if (original[i] == 0) {
                    continue;
                }
                int slot = destination[i];
                boolean merged = partner[i] != -1;
                if (slot == i && !merged) {
                    continue;
                }
                moves.add(new TileMove(
                        rowOf[i], colOf[i],
                        rowOf[slot], colOf[slot],
                        original[i], merged));
            }
            for (int i = 0; i < SIZE; i++) {
                cells[rowOf[i]][colOf[i]] = values[i];
            }
        }
        return new MoveResult(moved, gained, moves);
    }

    /**
     * 滑动核心：values 里是"从边缘算起"的一行数字，返回后 values 已变成滑动后的结果。
     * destination[i] 记录原始第 i 格最终落在哪个槽位，partner[i] 记录与之合并的另一格。
     */
    private static RowSlide slideCore(int[] values, int[] destination, int[] partner) {
        Arrays.fill(destination, -1);
        Arrays.fill(partner, -1);

        int[] source = new int[SIZE];
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            if (values[i] != 0) {
                source[count++] = i;
            }
        }

        int[] result = new int[SIZE];
        int read = 0;
        int write = 0;
        int gained = 0;
        while (read < count) {
            int value = values[source[read]];
            if (read + 1 < count && value == values[source[read + 1]]) {
                int merged = value * 2;
                result[write] = merged;
                gained += merged;
                destination[source[read]] = write;
                destination[source[read + 1]] = write;
                partner[source[read]] = source[read + 1];
                partner[source[read + 1]] = source[read];
                read += 2;
            } else {
                result[write] = value;
                destination[source[read]] = write;
                read += 1;
            }
            write++;
        }

        boolean moved = false;
        for (int i = 0; i < SIZE; i++) {
            if (values[i] != result[i]) {
                moved = true;
            }
            values[i] = result[i];
        }
        return new RowSlide(moved, gained);
    }
}
