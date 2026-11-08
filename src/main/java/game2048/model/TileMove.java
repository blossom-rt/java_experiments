package game2048.model;

/** 一次移动中单个方块的位移，用于滑行动画。 */
public final class TileMove {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final int value;
    private final boolean merged;

    public TileMove(int fromRow, int fromCol, int toRow, int toCol, int value, boolean merged) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.value = value;
        this.merged = merged;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public int getValue() {
        return value;
    }

    public boolean isMerged() {
        return merged;
    }
}
