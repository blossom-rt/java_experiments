import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Game2048 extends JFrame implements KeyListener {
    private int[][] board = new int[4][4];
    private static final int CELL_SIZE = 100;
    private static final int PADDING = 10;
    private static final int SCORE_HEIGHT = 60;
    private static final int GAP = 20;
    private Random random = new Random();
    private int score = 0;
    private boolean gameOver = false;
    private boolean win = false;
    private GamePanel gamePanel;

    public Game2048() {
        setTitle("2048");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        gamePanel = new GamePanel();
        int panelWidth = 4 * CELL_SIZE + 2 * PADDING;
        int panelHeight = SCORE_HEIGHT + GAP + 4 * CELL_SIZE + 2 * PADDING;
        gamePanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        addKeyListener(this);
        setFocusable(true);
        initGame();
    }

    private void initGame() {
        score = 0;
        gameOver = false;
        win = false;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                board[i][j] = 0;
            }
        }
        addRandomTile();
        addRandomTile();
    }

    private void addRandomTile() {
        int emptyCount = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) emptyCount++;
            }
        }
        if (emptyCount == 0) return;

        int target = random.nextInt(emptyCount);
        int count = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
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

    private boolean moveAndMerge(int[] row) {
        boolean moved = false;
        int[] compressed = new int[4];
        int index = 0;
        for (int num : row) {
            if (num != 0) {
                compressed[index++] = num;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (compressed[i] != 0 && compressed[i] == compressed[i + 1]) {
                compressed[i] *= 2;
                score += compressed[i];
                if (compressed[i] == 2048) win = true;
                compressed[i + 1] = 0;
                moved = true;
            }
        }
        int[] newRow = new int[4];
        index = 0;
        for (int num : compressed) {
            if (num != 0) {
                newRow[index++] = num;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (row[i] != newRow[i]) moved = true;
            row[i] = newRow[i];
        }
        return moved;
    }

    private void rotate() {
        int[][] newBoard = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                newBoard[i][j] = board[3 - j][i];
            }
        }
        board = newBoard;
    }

    private boolean isGameOver() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i < 3 && board[i][j] == board[i + 1][j]) return false;
                if (j < 3 && board[i][j] == board[i][j + 1]) return false;
            }
        }
        return true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) {
            initGame();
            gamePanel.repaint();
            return;
        }
        if (gameOver || win) return;

        boolean moved = false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                for (int i = 0; i < 4; i++) moved |= moveAndMerge(board[i]);
                break;
            case KeyEvent.VK_RIGHT:
                rotate(); rotate();
                for (int i = 0; i < 4; i++) moved |= moveAndMerge(board[i]);
                rotate(); rotate();
                break;
            case KeyEvent.VK_UP:
                rotate(); rotate(); rotate();
                for (int i = 0; i < 4; i++) moved |= moveAndMerge(board[i]);
                rotate();
                break;
            case KeyEvent.VK_DOWN:
                rotate();
                for (int i = 0; i < 4; i++) moved |= moveAndMerge(board[i]);
                rotate(); rotate(); rotate();
                break;
        }

        if (moved) {
            addRandomTile();
            if (isGameOver()) gameOver = true;
        }
        gamePanel.repaint();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int boardAreaWidth = 4 * CELL_SIZE + 2 * PADDING;
            int boardStartX = (getWidth() - boardAreaWidth) / 2;

            // 分数栏
            g2d.setColor(new Color(187, 173, 160));
            g2d.fillRoundRect(boardStartX, 0, boardAreaWidth, SCORE_HEIGHT, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("黑体", Font.BOLD, 28));
            String scoreText = "分数: " + score;
            FontMetrics fm = g2d.getFontMetrics();
            int scoreTextX = boardStartX + 20;
            int scoreTextY = (SCORE_HEIGHT + fm.getAscent()) / 2 - 2;
            g2d.drawString(scoreText, scoreTextX, scoreTextY);

            // 棋盘
            int boardY = SCORE_HEIGHT + GAP;
            g2d.setColor(new Color(187, 173, 160));
            g2d.fillRoundRect(boardStartX, boardY, boardAreaWidth, boardAreaWidth, 10, 10);

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    drawTile(g2d, boardStartX, boardY, i, j, board[i][j]);
                }
            }

            // 半透明遮罩层（游戏结束 / 通关）
            if (gameOver) {
                drawOverlay(g2d, "游戏结束！按 R 重新开始", new Color(119, 110, 101));
            } else if (win) {
                drawOverlay(g2d, "恭喜通关！按 R 重新开始", new Color(119, 110, 101));
            }
        }

        private void drawTile(Graphics2D g2d, int boardX, int boardY, int row, int col, int value) {
            int x = boardX + PADDING + col * CELL_SIZE;
            int y = boardY + PADDING + row * CELL_SIZE;
            int size = CELL_SIZE - 2 * PADDING;

            g2d.setColor(getTileColor(value));
            g2d.fillRoundRect(x, y, size, size, 8, 8);

            if (value != 0) {
                g2d.setColor(value <= 4 ? new Color(119, 110, 101) : Color.WHITE);
                g2d.setFont(new Font("黑体", Font.BOLD, value < 100 ? 36 : value < 1000 ? 30 : 24));
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
                default:   return new Color(60, 58, 50);
            }
        }

        private void drawOverlay(Graphics2D g2d, String text, Color color) {
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(color);
            g2d.setFont(new Font("黑体", Font.BOLD, 22));
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = getHeight() / 2 + fm.getAscent() / 2;
            g2d.drawString(text, x, y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game2048().setVisible(true));
    }
}
