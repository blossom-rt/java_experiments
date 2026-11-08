package game2048.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighScoreStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void missingFileReadsZero() {
        HighScoreStore store = new HighScoreStore(tempDir.resolve("nope.txt"));
        assertEquals(0, store.load());
    }

    @Test
    void savedScoreRoundTrips() {
        Path file = tempDir.resolve("best.txt");
        HighScoreStore store = new HighScoreStore(file);
        store.save(1234);
        assertEquals(1234, store.load());
    }

    @Test
    void saveCreatesMissingDirectories() {
        Path file = tempDir.resolve("nested/deeper/best.txt");
        HighScoreStore store = new HighScoreStore(file);
        store.save(88);
        assertEquals(88, store.load());
    }

    @Test
    void corruptFileReadsZero() throws IOException {
        Path file = tempDir.resolve("broken.txt");
        Files.write(file, "not-a-number".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, new HighScoreStore(file).load());
    }

    @Test
    void negativeValueReadsZero() throws IOException {
        Path file = tempDir.resolve("negative.txt");
        Files.write(file, "-5".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, new HighScoreStore(file).load());
    }
}
