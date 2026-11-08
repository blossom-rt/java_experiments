package game2048;

import game2048.model.Difficulty;
import game2048.model.Game;
import game2048.storage.HighScoreStore;
import game2048.view.GameFrame;

import javax.swing.SwingUtilities;
import java.util.Random;

/** 程序入口。 */
public final class Game2048 {
    private Game2048() {
    }

    public static Game newGame() {
        return new Game(new Random(), HighScoreStore.defaultStore(), Difficulty.NORMAL);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame(newGame()).setVisible(true));
    }
}
