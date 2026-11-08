package game2048.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 最高分的本地持久化，默认存到用户目录下的 .game2048-highscore。 */
public final class HighScoreStore {
    private final Path file;

    public HighScoreStore(Path file) {
        this.file = file;
    }

    public static HighScoreStore defaultStore() {
        return new HighScoreStore(Paths.get(System.getProperty("user.home"), ".game2048-highscore"));
    }

    public Path getFile() {
        return file;
    }

    public int load() {
        try {
            if (!Files.exists(file)) {
                return 0;
            }
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
            return Math.max(0, Integer.parseInt(text));
        } catch (IOException e) {
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean save(int best) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, String.valueOf(best).getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
