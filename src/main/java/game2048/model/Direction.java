package game2048.model;

/**
 * 四个移动方向。rowAt / colAt 把某一行（列）上的第 index 个格子映射回棋盘坐标，
 * index 指的是"从移动方向的边缘算起"的第几个格子。
 */
public enum Direction {
    LEFT("←"),
    RIGHT("→"),
    UP("↑"),
    DOWN("↓");

    private final String symbol;

    Direction(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public int rowAt(int line, int index) {
        switch (this) {
            case UP:
                return index;
            case DOWN:
                return Board.SIZE - 1 - index;
            default:
                return line;
        }
    }

    public int colAt(int line, int index) {
        switch (this) {
            case LEFT:
                return index;
            case RIGHT:
                return Board.SIZE - 1 - index;
            default:
                return line;
        }
    }
}
