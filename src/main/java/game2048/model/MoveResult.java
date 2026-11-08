package game2048.model;

import java.util.Collections;
import java.util.List;

/** 一次移动的结果：是否有效、得分、以及用于动画的方块位移列表。 */
public final class MoveResult {
    public static final MoveResult NONE = new MoveResult(false, 0, Collections.<TileMove>emptyList());

    private final boolean moved;
    private final int gained;
    private final List<TileMove> moves;

    public MoveResult(boolean moved, int gained, List<TileMove> moves) {
        this.moved = moved;
        this.gained = gained;
        this.moves = Collections.unmodifiableList(moves);
    }

    public boolean isMoved() {
        return moved;
    }

    public int getGained() {
        return gained;
    }

    public List<TileMove> getMoves() {
        return moves;
    }
}
