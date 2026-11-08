package game2048.model;

/** 单行滑动/合并的结果。 */
public final class RowSlide {
    private final boolean moved;
    private final int gained;

    public RowSlide(boolean moved, int gained) {
        this.moved = moved;
        this.gained = gained;
    }

    public boolean isMoved() {
        return moved;
    }

    public int getGained() {
        return gained;
    }
}
