package game2048.view;

import game2048.model.Board;
import game2048.model.Direction;
import game2048.model.Game;
import game2048.model.TileMove;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.List;

/** 棋盘绘制、方块滑动动画、鼠标拖拽与方向提示。 */
public final class BoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final int TILE_SIZE = 90;
    public static final int TILE_GAP = 10;
    public static final int SCORE_HEIGHT = 60;
    public static final int SCORE_GAP = 20;
    public static final int BOARD_SIZE = Board.SIZE * TILE_SIZE + (Board.SIZE + 1) * TILE_GAP;
    public static final int BOARD_TOP = SCORE_HEIGHT + SCORE_GAP;
    public static final int PANEL_WIDTH = BOARD_SIZE;
    public static final int PANEL_HEIGHT = BOARD_TOP + BOARD_SIZE;

    private static final int ANIMATION_MS = 110;
    private static final int DRAG_THRESHOLD = 25;

    /** 面板把用户输入翻译成"往哪走"，由窗口决定怎么处理。 */
    public interface InputHandler {
        void move(Direction direction);
    }

    private final Game game;
    private final Timer animationTimer;

    private InputHandler inputHandler;
    private List<TileMove> animatingMoves;
    private long animationStart;
    private Direction hint;
    private long hintShownAt;

    private int dragStartX = -1;
    private int dragStartY = -1;

    public BoardPanel(Game game) {
        this.game = game;
        setOpaque(true);
        setLayout(null);
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        installMouseDrag();
        animationTimer = new Timer(16, e -> onAnimationTick());
    }

    private void onAnimationTick() {
        if (System.currentTimeMillis() - animationStart >= ANIMATION_MS) {
            animatingMoves = null;
            animationTimer.stop();
        }
        repaint();
    }

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void animate(List<TileMove> moves) {
        if (moves == null || moves.isEmpty()) {
            repaint();
            return;
        }
        animatingMoves = moves;
        animationStart = System.currentTimeMillis();
        animationTimer.start();
    }

    public void showHint(Direction direction) {
        hint = direction;
        hintShownAt = System.currentTimeMillis();
        repaint();
    }

    public void clearHint() {
        hint = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Theme.background());
        g.fillRect(0, 0, getWidth(), getHeight());

        paintScoreBar(g);
        paintBoard(g);
        paintAnimation(g);
        paintOverlay(g);
        paintHint(g);

        g.dispose();
    }

    private void paintScoreBar(Graphics2D g) {
        g.setColor(Theme.scoreBar());
        g.fillRoundRect(0, 0, PANEL_WIDTH, SCORE_HEIGHT, 10, 10);

        g.setColor(Theme.scoreText());
        g.setFont(Theme.uiFont(24));
        g.drawString("分数 " + game.score(), 18, 34);

        g.setColor(Theme.bestText());
        g.setFont(Theme.uiFont(12));
        g.drawString("最高 " + game.best(), 18, 53);
    }

    private void paintBoard(Graphics2D g) {
        g.setColor(Theme.boardBackground());
        g.fillRoundRect(0, BOARD_TOP, BOARD_SIZE, BOARD_SIZE, 10, 10);

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                int value = game.board().get(row, col);
                boolean hidden = value != 0 && isAnimating() && isMergeTarget(row, col);
                if (value == 0 || hidden) {
                    g.setColor(Theme.emptyCell());
                    g.fillRoundRect(cellX(col), cellY(row), TILE_SIZE, TILE_SIZE, 6, 6);
                } else {
                    drawTile(g, value, cellX(col), cellY(row));
                }
            }
        }
    }

    private void paintAnimation(Graphics2D g) {
        if (!isAnimating()) {
            return;
        }
        double progress = Math.min(1.0, (System.currentTimeMillis() - animationStart) / (double) ANIMATION_MS);
        double eased = 1 - Math.pow(1 - progress, 3);
        for (TileMove move : animatingMoves) {
            double fromX = cellX(move.getFromCol());
            double fromY = cellY(move.getFromRow());
            double toX = cellX(move.getToCol());
            double toY = cellY(move.getToRow());
            drawTile(g, move.getValue(), fromX + (toX - fromX) * eased, fromY + (toY - fromY) * eased);
        }
    }

    private void paintOverlay(Graphics2D g) {
        if (game.status() == Game.Status.PLAYING) {
            return;
        }
        Composite original = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
        g.setColor(Theme.overlayWash());
        g.fillRoundRect(2, BOARD_TOP + 2, BOARD_SIZE - 4, BOARD_SIZE - 4, 10, 10);
        g.setComposite(original);

        g.setColor(Theme.overlayText());
        g.setFont(Theme.uiFont(22));
        String title = game.status() == Game.Status.WON ? "恭喜通关 2048！" : "游戏结束！";
        String subtitle = game.status() == Game.Status.WON ? "按 C 继续挑战" : "按 R 重新开始";
        FontMetrics titleMetrics = g.getFontMetrics();
        FontMetrics subtitleMetrics = g.getFontMetrics(Theme.uiFont(16));
        int centerX = BOARD_SIZE / 2;
        int centerY = BOARD_TOP + BOARD_SIZE / 2;
        g.drawString(title, centerX - titleMetrics.stringWidth(title) / 2,
                centerY - 6);
        g.setFont(Theme.uiFont(16));
        g.drawString(subtitle, centerX - subtitleMetrics.stringWidth(subtitle) / 2,
                centerY + 24);
    }

    private void paintHint(Graphics2D g) {
        if (hint == null) {
            return;
        }
        if (System.currentTimeMillis() - hintShownAt > 3000) {
            hint = null;
            return;
        }
        int cx = BOARD_SIZE / 2;
        int cy = BOARD_TOP + BOARD_SIZE / 2;
        int depth = 30;
        int spread = 18;
        Path2D.Double arrow = new Path2D.Double();
        switch (hint) {
            case LEFT:
                arrow.moveTo(8, cy);
                arrow.lineTo(8 + depth, cy - spread);
                arrow.lineTo(8 + depth, cy + spread);
                break;
            case RIGHT:
                arrow.moveTo(BOARD_SIZE - 8, cy);
                arrow.lineTo(BOARD_SIZE - 8 - depth, cy - spread);
                arrow.lineTo(BOARD_SIZE - 8 - depth, cy + spread);
                break;
            case UP:
                arrow.moveTo(cx, BOARD_TOP + 8);
                arrow.lineTo(cx - spread, BOARD_TOP + 8 + depth);
                arrow.lineTo(cx + spread, BOARD_TOP + 8 + depth);
                break;
            default:
                arrow.moveTo(cx, BOARD_TOP + BOARD_SIZE - 8);
                arrow.lineTo(cx - spread, BOARD_TOP + BOARD_SIZE - 8 - depth);
                arrow.lineTo(cx + spread, BOARD_TOP + BOARD_SIZE - 8 - depth);
                break;
        }
        arrow.closePath();
        Color base = Theme.hint();
        g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 190));
        g.fill(arrow);
        g.setColor(new Color(0, 0, 0, 90));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(arrow);
    }

    private void drawTile(Graphics2D g, int value, double x, double y) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        g.setColor(Theme.tileColor(value));
        g.fillRoundRect(px, py, TILE_SIZE, TILE_SIZE, 6, 6);

        g.setColor(Theme.tileTextColor(value));
        g.setFont(Theme.uiFont(tileFontSize(value)));
        FontMetrics metrics = g.getFontMetrics();
        String text = String.valueOf(value);
        int textX = px + (TILE_SIZE - metrics.stringWidth(text)) / 2;
        int textY = py + (TILE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(text, textX, textY);
    }

    private static int tileFontSize(int value) {
        if (value < 100) {
            return 40;
        }
        if (value < 1000) {
            return 34;
        }
        if (value < 10000) {
            return 28;
        }
        return 24;
    }

    private boolean isAnimating() {
        return animatingMoves != null && animationTimer.isRunning();
    }

    private boolean isMergeTarget(int row, int col) {
        for (TileMove move : animatingMoves) {
            if (move.isMerged() && move.getToRow() == row && move.getToCol() == col) {
                return true;
            }
        }
        return false;
    }

    public static int cellX(int col) {
        return TILE_GAP + col * (TILE_SIZE + TILE_GAP);
    }

    public static int cellY(int row) {
        return BOARD_TOP + TILE_GAP + row * (TILE_SIZE + TILE_GAP);
    }

    private void installMouseDrag() {
        MouseAdapter drag = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartX = e.getX();
                dragStartY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStartX < 0 || inputHandler == null) {
                    return;
                }
                int dx = e.getX() - dragStartX;
                int dy = e.getY() - dragStartY;
                dragStartX = -1;
                if (Math.max(Math.abs(dx), Math.abs(dy)) < DRAG_THRESHOLD) {
                    return;
                }
                Direction direction;
                if (Math.abs(dx) > Math.abs(dy)) {
                    direction = dx > 0 ? Direction.RIGHT : Direction.LEFT;
                } else {
                    direction = dy > 0 ? Direction.DOWN : Direction.UP;
                }
                clearHint();
                inputHandler.move(direction);
            }
        };
        addMouseListener(drag);
    }
}
