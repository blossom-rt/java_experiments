import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

public final class Game2048 extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int SIZE = 4;
    private static final int TILE_SIZE = 90;
    private static final int TILE_GAP = 10;
    private static final int SCORE_HEIGHT = 60;
    private static final int SCORE_GAP = 20;
    private static final int BOARD_SIZE = SIZE * TILE_SIZE + (SIZE + 1) * TILE_GAP;
    private static final String UI_FONT_FAMILY = pickUiFontFamily();

    private int[][] board = new int[SIZE][SIZE];
    private final Random random = new Random();
    private int score = 0;
    private boolean gameOver = false;
    private boolean win = false;
    private boolean won = false;
    private GamePanel gamePanel;

    // 优先使用系统中确实存在的黑体/雅黑，找不到时退回逻辑字体，避免硬编码字体名在别的系统上变成方块
    private static String pickUiFontFamily() {
        String[] candidates = {"微软雅黑", "Microsoft YaHei", "黑体", "SimHei"};
        java.util.List<String> available = java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String candidate : candidates) {
            if (available.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
    }

    private static Font uiFont(int size) {
        return new Font(UI_FONT_FAMILY, Font.BOLD, size);
    }

    public Game2048() {
        setTitle("2048");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(BOARD_SIZE, SCORE_HEIGHT + SCORE_GAP + BOARD_SIZE));
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        bindKeys();
        initGame();
    }

    private void initGame() {
        score = 0;
        gameOver = false;
        win = false;
        won = false;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = 0;
            }
        }
        addRandomTile();
        addRandomTile();
        gamePanel.repaint();
    }

    private void addRandomTile() {
        int emptyCount = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) emptyCount++;
            }
        }
        if (emptyCount == 0) return;

        int target = random.nextInt(emptyCount);
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    if (count == target) {
                        board[i][j] = random.nextBoolean() ? 2 : 4;
                        return;
                    }
                    count++;
                }
            }
        }
    }

    // 纯函数：只重排/合并这一行，返回是否移动过以及本次得分，不改动全局状态
    private MoveResult slideRow(int[] row) {
        int[] compressed = new int[SIZE];
        int index = 0;
        for (int value : row) {
            if (value != 0) {
                compressed[index++] = value;
            }
        }

        int gained = 0;
        for (int i = 0; i < SIZE - 1; i++) {
            if (compressed[i] != 0 && compressed[i] == compressed[i + 1]) {
                compressed[i] *= 2;
                gained += compressed[i];
                compressed[i + 1] = 0;
            }
        }

        int[] newRow = new int[SIZE];
        index = 0;
        for (int value : compressed) {
            if (value != 0) {
                newRow[index++] = value;
            }
        }

        boolean moved = false;
        for (int i = 0; i < SIZE; i++) {
            if (row[i] != newRow[i]) {
                moved = true;
            }
            row[i] = newRow[i];
        }
        return new MoveResult(moved, gained);
    }

    private boolean hasTile(int value) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class MoveResult {
        private final boolean moved;
        private final int gained;

        MoveResult(boolean moved, int gained) {
            this.moved = moved;
            this.gained = gained;
        }
    }

    private void rotate() {
        int[][] newBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                newBoard[i][j] = board[SIZE - 1 - j][i];
            }
        }
        board = newBoard;
    }

    private boolean isGameOver() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i < SIZE - 1 && board[i][j] == board[i + 1][j]) return false;
                if (j < SIZE - 1 && board[i][j] == board[i][j + 1]) return false;
            }
        }
        return true;
    }

    private enum Direction { LEFT, RIGHT, UP, DOWN }

    private void bindKeys() {
        bindKey("move-left", KeyEvent.VK_LEFT, () -> move(Direction.LEFT));
        bindKey("move-right", KeyEvent.VK_RIGHT, () -> move(Direction.RIGHT));
        bindKey("move-up", KeyEvent.VK_UP, () -> move(Direction.UP));
        bindKey("move-down", KeyEvent.VK_DOWN, () -> move(Direction.DOWN));
        bindKey("restart", KeyEvent.VK_R, this::restartGame);
        bindKey("continue", KeyEvent.VK_C, this::continueGame);
    }

    private void bindKey(String name, int keyCode, Runnable action) {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), name);
        getRootPane().getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void move(Direction direction) {
        if (gameOver || win) return;

        int rotations = rotationsFor(direction);
        for (int i = 0; i < rotations; i++) rotate();

        boolean moved = false;
        int gained = 0;
        for (int i = 0; i < SIZE; i++) {
            MoveResult result = slideRow(board[i]);
            moved |= result.moved;
            gained += result.gained;
        }
        for (int i = 0; i < (SIZE - rotations) % SIZE; i++) rotate();

        if (moved) {
            score += gained;
            if (!won && hasTile(2048)) {
                won = true;
                win = true;
            }
        }

        // 通关那一手定格：不再随机补方块，等玩家按 C 继续或 R 重开
        if (moved && !win) {
            addRandomTile();
            if (isGameOver()) gameOver = true;
        }
        gamePanel.repaint();
    }

    private int rotationsFor(Direction direction) {
        switch (direction) {
            case RIGHT: return 2;
            case UP:    return 3;
            case DOWN:  return 1;
            default:    return 0;
        }
    }

    private void restartGame() {
        initGame();
    }

    private void continueGame() {
        if (!win) return;
        win = false;
        gameOver = isGameOver();
        gamePanel.repaint();
    }

    class GamePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        GamePanel() {
            setLayout(null);
            RestartButton restartButton = new RestartButton("重新开始");
            restartButton.setBounds(BOARD_SIZE - 106, 13, 96, 34);
            restartButton.setToolTipText("重开一局（快捷键 R）");
            restartButton.addActionListener(e -> restartGame());
            add(restartButton);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int boardStartX = (getWidth() - BOARD_SIZE) / 2;

            // 分数栏
            g2d.setColor(new Color(187, 173, 160));
            g2d.fillRoundRect(boardStartX, 0, BOARD_SIZE, SCORE_HEIGHT, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.setFont(uiFont(28));
            String scoreText = "分数: " + score;
            FontMetrics fm = g2d.getFontMetrics();
            int scoreTextX = boardStartX + 20;
            int scoreTextY = (SCORE_HEIGHT + fm.getAscent()) / 2 - 2;
            g2d.drawString(scoreText, scoreTextX, scoreTextY);

            // 棋盘
            int boardY = SCORE_HEIGHT + SCORE_GAP;
            g2d.setColor(new Color(187, 173, 160));
            g2d.fillRoundRect(boardStartX, boardY, BOARD_SIZE, BOARD_SIZE, 10, 10);

            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    drawTile(g2d, boardStartX, boardY, i, j, board[i][j]);
                }
            }

            // 半透明遮罩层（游戏结束 / 通关）
            if (gameOver) {
                drawOverlay(g2d, new Color(119, 110, 101), "游戏结束！", "按 R 重新开始");
            } else if (win) {
                drawOverlay(g2d, new Color(119, 110, 101), "恭喜通关 2048！", "按 C 继续挑战，按 R 重新开始");
            }
        }

        private void drawTile(Graphics2D g2d, int boardX, int boardY, int row, int col, int value) {
            int x = boardX + TILE_GAP + col * (TILE_SIZE + TILE_GAP);
            int y = boardY + TILE_GAP + row * (TILE_SIZE + TILE_GAP);
            int size = TILE_SIZE;

            g2d.setColor(getTileColor(value));
            g2d.fillRoundRect(x, y, size, size, 8, 8);

            if (value != 0) {
                g2d.setColor(value <= 4 ? new Color(119, 110, 101) : Color.WHITE);
                g2d.setFont(uiFont(value < 100 ? 36 : value < 1000 ? 30 : 24));
                String text = String.valueOf(value);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (size - fm.stringWidth(text)) / 2;
                int textY = y + (size + fm.getAscent()) / 2 - 4;
                g2d.drawString(text, textX, textY);
            }
        }

        private Color getTileColor(int value) {
            switch (value) {
                case 0:    return new Color(205, 193, 180);
                case 2:    return new Color(238, 228, 218);
                case 4:    return new Color(237, 224, 200);
                case 8:    return new Color(242, 177, 121);
                case 16:   return new Color(245, 149, 99);
                case 32:   return new Color(246, 124, 95);
                case 64:   return new Color(246, 94, 59);
                case 128:  return new Color(237, 207, 114);
                case 256:  return new Color(237, 204, 97);
                case 512:  return new Color(237, 200, 80);
                case 1024: return new Color(237, 197, 63);
                case 2048: return new Color(237, 194, 46);
                case 4096: return new Color(239, 180, 30);
                case 8192: return new Color(242, 160, 20);
                default:   return new Color(60, 58, 50);
            }
        }

        private void drawOverlay(Graphics2D g2d, Color color, String... lines) {
            int top = SCORE_HEIGHT + SCORE_GAP;
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRoundRect(0, top, getWidth(), getHeight() - top, 10, 10);
            g2d.setColor(color);
            g2d.setFont(uiFont(22));
            FontMetrics fm = g2d.getFontMetrics();
            int lineHeight = fm.getHeight();
            int centerY = top + (getHeight() - top) / 2;
            int firstBaseline = centerY + (fm.getAscent() - (lines.length - 1) * lineHeight) / 2;
            for (int i = 0; i < lines.length; i++) {
                int x = (getWidth() - fm.stringWidth(lines[i])) / 2;
                g2d.drawString(lines[i], x, firstBaseline + i * lineHeight);
            }
        }

        // 圆角自绘按钮：Metal 外观下 JButton 的背景色不可靠，这里自己画
        private class RestartButton extends JButton {
            private static final long serialVersionUID = 1L;

            RestartButton(String text) {
                super(text);
                setFont(uiFont(15));
                setForeground(Color.WHITE);
                setFocusable(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Color background = new Color(143, 122, 102);
                if (getModel().isPressed()) {
                    background = new Color(120, 101, 84);
                } else if (getModel().isRollover()) {
                    background = new Color(160, 138, 120);
                }
                g2d.setColor(background);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent()) / 2 - 3;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game2048().setVisible(true));
    }
}
