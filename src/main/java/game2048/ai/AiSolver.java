package game2048.ai;

import game2048.model.Board;
import game2048.model.Direction;
import game2048.model.MoveResult;

/**
 * 期望最大化（expectimax）搜索，用于"提示"和"自动演示"。
 * 只用棋盘本身做评估，不依赖界面，便于单元测试。
 */
public final class AiSolver {
    private static final int DEPTH = 2;
    private static final double FOUR_PROBABILITY = 0.1;

    /** 返回建议的移动方向；无路可走时返回 null。 */
    public Direction suggest(Board board) {
        Direction best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (Direction direction : Direction.values()) {
            Board after = board.copy();
            MoveResult result = after.move(direction);
            if (!result.isMoved()) {
                continue;
            }
            double value = chanceValue(after, DEPTH);
            if (value > bestValue) {
                bestValue = value;
                best = direction;
            }
        }
        return best;
    }

    private double chanceValue(Board board, int depth) {
        int empty = board.emptyCount();
        if (depth <= 1 || empty == 0) {
            return evaluate(board);
        }
        double total = 0;
        int cells = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.get(row, col) != 0) {
                    continue;
                }
                cells++;
                Board withTwo = board.copy();
                withTwo.set(row, col, 2);
                total += (1 - FOUR_PROBABILITY) * maxValue(withTwo, depth - 1);

                Board withFour = board.copy();
                withFour.set(row, col, 4);
                total += FOUR_PROBABILITY * maxValue(withFour, depth - 1);
            }
        }
        return cells == 0 ? evaluate(board) : total / cells;
    }

    private double maxValue(Board board, int depth) {
        double best = Double.NEGATIVE_INFINITY;
        boolean any = false;
        for (Direction direction : Direction.values()) {
            Board after = board.copy();
            if (!after.move(direction).isMoved()) {
                continue;
            }
            any = true;
            double value = chanceValue(after, depth);
            if (value > best) {
                best = value;
            }
        }
        return any ? best : evaluate(board);
    }

    static double evaluate(Board board) {
        double empties = board.emptyCount();
        double smoothness = 0;
        double monotonicity = 0;
        int maxLog = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            int[] row = new int[Board.SIZE];
            int[] col = new int[Board.SIZE];
            for (int j = 0; j < Board.SIZE; j++) {
                row[j] = board.get(i, j);
                col[j] = board.get(j, i);
                maxLog = Math.max(maxLog, log2(row[j]));
                maxLog = Math.max(maxLog, log2(col[j]));
            }
            smoothness += smoothness(row) + smoothness(col);
            monotonicity += monotonicity(row) + monotonicity(col);
        }
        return empties * 3.0 + smoothness * 0.1 + monotonicity * 0.3 + maxLog;
    }

    private static double smoothness(int[] line) {
        double total = 0;
        for (int i = 0; i + 1 < line.length; i++) {
            if (line[i] == 0 || line[i + 1] == 0) {
                continue;
            }
            total -= Math.abs(log2(line[i]) - log2(line[i + 1]));
        }
        return total;
    }

    private static double monotonicity(int[] line) {
        double increasing = 0;
        double decreasing = 0;
        for (int i = 0; i + 1 < line.length; i++) {
            if (line[i] == 0 || line[i + 1] == 0) {
                continue;
            }
            int a = log2(line[i]);
            int b = log2(line[i + 1]);
            if (a > b) {
                decreasing += a - b;
            } else {
                increasing += b - a;
            }
        }
        return -Math.max(increasing, decreasing);
    }

    private static int log2(int value) {
        return value <= 0 ? 0 : 31 - Integer.numberOfLeadingZeros(value);
    }
}
