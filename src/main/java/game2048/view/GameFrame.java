package game2048.view;

import game2048.search.SearchSolver;
import game2048.model.Difficulty;
import game2048.model.Direction;
import game2048.model.Game;
import game2048.model.MoveResult;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/** 主窗口：装配棋盘面板、按钮、菜单与全部快捷键。 */
public final class GameFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final int DEMO_DELAY_MS = 120;

    private final Game game;
    private final BoardPanel panel;
    private final SoundPlayer sound = new SoundPlayer();
    private SearchSolver solver = new SearchSolver();

    private final Timer demoTimer;
    private final JMenuItem undoItem;
    private final JMenuItem continueItem;
    private final JCheckBoxMenuItem demoItem;
    private JCheckBoxMenuItem soundItem;
    private final Map<Integer, Runnable> keyActions = new HashMap<Integer, Runnable>();
    private boolean demoRunning;

    public GameFrame(Game game) {
        super("2048");
        this.game = game;
        this.panel = new BoardPanel(game);
        panel.setInputHandler(this::handleMove);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(panel);

        FlatButton undoButton = new FlatButton("撤销", this::undo);
        undoButton.setBounds(204, 13, 92, 34);
        undoButton.setToolTipText("撤销一步（快捷键 U）");
        panel.add(undoButton);

        FlatButton restartButton = new FlatButton("重新开始", this::restart);
        restartButton.setBounds(304, 13, 96, 34);
        restartButton.setToolTipText("重开一局（快捷键 R）");
        panel.add(restartButton);

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("游戏");

        undoItem = new JMenuItem("撤销一步 (U)");
        undoItem.addActionListener(e -> undo());
        gameMenu.add(undoItem);

        continueItem = new JMenuItem("继续挑战 (C)");
        continueItem.addActionListener(e -> continueAfterWin());
        gameMenu.add(continueItem);

        JMenuItem restartItem = new JMenuItem("重新开始 (R)");
        restartItem.addActionListener(e -> restart());
        gameMenu.add(restartItem);

        JMenuItem hintItem = new JMenuItem("方向提示一步 (H)");
        hintItem.addActionListener(e -> showHint());
        gameMenu.add(hintItem);

        demoItem = new JCheckBoxMenuItem("自动演示 (P)");
        demoItem.addActionListener(e -> setDemo(demoItem.isSelected()));
        gameMenu.add(demoItem);

        menuBar.add(gameMenu);
        menuBar.add(buildSettingsMenu());
        setJMenuBar(menuBar);

        demoTimer = new Timer(DEMO_DELAY_MS, e -> demoTick());

        installKeys();
        refreshMenuState();
        pack();
        setLocationRelativeTo(null);
    }

    private JMenu buildSettingsMenu() {
        JMenu settings = new JMenu("设置");

        JMenu difficultyMenu = new JMenu("难度");
        ButtonGroup difficultyGroup = new ButtonGroup();
        for (Difficulty difficulty : Difficulty.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(difficulty.label());
            item.setSelected(difficulty == game.difficulty());
            item.addActionListener(e -> {
                game.setDifficulty(difficulty);
                setTitle();
            });
            difficultyGroup.add(item);
            difficultyMenu.add(item);
        }
        settings.add(difficultyMenu);

        JMenu themeMenu = new JMenu("主题");
        ButtonGroup themeGroup = new ButtonGroup();
        JRadioButtonMenuItem light = new JRadioButtonMenuItem("明亮 (T)");
        light.setSelected(Theme.mode() == Theme.Mode.LIGHT);
        light.addActionListener(e -> {
            Theme.setMode(Theme.Mode.LIGHT);
            panel.repaint();
        });
        JRadioButtonMenuItem dark = new JRadioButtonMenuItem("暗黑 (T)");
        dark.addActionListener(e -> {
            Theme.setMode(Theme.Mode.DARK);
            panel.repaint();
        });
        themeGroup.add(light);
        themeGroup.add(dark);
        themeMenu.add(light);
        themeMenu.add(dark);
        settings.add(themeMenu);

        soundItem = new JCheckBoxMenuItem("音效 (M)", true);
        soundItem.addActionListener(e -> sound.setEnabled(soundItem.isSelected()));
        settings.add(soundItem);

        settings.addSeparator();
        JMenu depthMenu = new JMenu("搜索深度");
        ButtonGroup depthGroup = new ButtonGroup();
        for (int depth : new int[] {3, 4}) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(depth == 3
                    ? "3 层（默认，快）"
                    : "4 层（慢，块更大）");
            item.setSelected(depth == solver.depth());
            item.addActionListener(e -> solver = new SearchSolver(depth));
            depthGroup.add(item);
            depthMenu.add(item);
        }
        settings.add(depthMenu);

        return settings;
    }

    private void installKeys() {
        keyActions.put(KeyEvent.VK_LEFT, () -> handleMove(Direction.LEFT));
        keyActions.put(KeyEvent.VK_RIGHT, () -> handleMove(Direction.RIGHT));
        keyActions.put(KeyEvent.VK_UP, () -> handleMove(Direction.UP));
        keyActions.put(KeyEvent.VK_DOWN, () -> handleMove(Direction.DOWN));
        keyActions.put(KeyEvent.VK_A, () -> handleMove(Direction.LEFT));
        keyActions.put(KeyEvent.VK_D, () -> handleMove(Direction.RIGHT));
        keyActions.put(KeyEvent.VK_W, () -> handleMove(Direction.UP));
        keyActions.put(KeyEvent.VK_S, () -> handleMove(Direction.DOWN));
        keyActions.put(KeyEvent.VK_U, this::undo);
        keyActions.put(KeyEvent.VK_Z, this::undo);
        keyActions.put(KeyEvent.VK_R, this::restart);
        keyActions.put(KeyEvent.VK_C, this::continueAfterWin);
        keyActions.put(KeyEvent.VK_H, this::showHint);
        keyActions.put(KeyEvent.VK_P, () -> setDemo(!demoRunning));
        keyActions.put(KeyEvent.VK_M, () -> {
            sound.setEnabled(!sound.isEnabled());
            soundItem.setSelected(sound.isEnabled());
        });
        keyActions.put(KeyEvent.VK_T, () -> {
            Theme.toggle();
            panel.repaint();
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event ->
                event.getID() == KeyEvent.KEY_PRESSED
                        && belongsToThisWindow(event.getComponent())
                        && handleShortcut(event.getKeyCode()));
    }

    /** 快捷键入口：键盘事件与界面测试都走这里。 */
    boolean handleShortcut(int keyCode) {
        Runnable action = keyActions.get(keyCode);
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    private boolean belongsToThisWindow(Component component) {
        if (component == null) {
            return false;
        }
        Window window = component instanceof Window
                ? (Window) component
                : SwingUtilities.getWindowAncestor(component);
        return window == this;
    }

    private void handleMove(Direction direction) {
        stopDemo();
        performMove(direction, false);
    }

    private void performMove(Direction direction, boolean silent) {
        panel.clearHint();
        MoveResult result = game.move(direction);
        if (!result.isMoved()) {
            return;
        }
        panel.animate(result.getMoves());
        if (!silent) {
            if (result.getGained() > 0) {
                sound.play(SoundPlayer.Sound.MERGE);
            } else {
                sound.play(SoundPlayer.Sound.MOVE);
            }
            if (game.status() == Game.Status.WON) {
                sound.play(SoundPlayer.Sound.WIN);
            } else if (game.status() == Game.Status.OVER) {
                sound.play(SoundPlayer.Sound.OVER);
            }
        }
        refreshMenuState();
        panel.repaint();
    }

    private void undo() {
        stopDemo();
        if (game.undo()) {
            sound.play(SoundPlayer.Sound.MOVE);
            refreshMenuState();
            panel.repaint();
        }
    }

    private void restart() {
        stopDemo();
        game.restart();
        panel.clearHint();
        refreshMenuState();
        panel.repaint();
    }

    private void continueAfterWin() {
        if (game.status() != Game.Status.WON) {
            return;
        }
        game.continueAfterWin();
        refreshMenuState();
        panel.repaint();
    }

    private void showHint() {
        stopDemo();
        Direction suggestion = solver.suggest(game.board());
        if (suggestion != null) {
            panel.showHint(suggestion);
        }
    }

    private void setDemo(boolean running) {
        if (running == demoRunning) {
            return;
        }
        demoRunning = running;
        demoItem.setSelected(running);
        if (running) {
            demoTimer.start();
        } else {
            demoTimer.stop();
        }
    }

    private void stopDemo() {
        if (demoRunning) {
            demoRunning = false;
            demoItem.setSelected(false);
            demoTimer.stop();
        }
    }

    private void demoTick() {
        if (!demoRunning) {
            return;
        }
        if (game.status() == Game.Status.WON) {
            game.continueAfterWin();
        }
        if (game.status() == Game.Status.OVER) {
            game.restart();
            refreshMenuState();
            panel.repaint();
            return;
        }
        Direction suggestion = solver.suggest(game.board());
        if (suggestion != null) {
            performMove(suggestion, true);
        }
    }

    private void refreshMenuState() {
        undoItem.setEnabled(game.canUndo());
        continueItem.setEnabled(game.status() == Game.Status.WON);
        setTitle();
    }

    private void setTitle() {
        setTitle("2048 · " + game.difficulty().label() + " · 最高分 " + game.best());
    }

    /** 供测试或嵌入使用：当前棋盘面板。 */
    public BoardPanel panel() {
        return panel;
    }
}
