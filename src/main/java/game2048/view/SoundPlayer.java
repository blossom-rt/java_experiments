package game2048.view;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 极简音效：用正弦波现场合成，不依赖任何音频资源文件。
 * 没有可用音频设备时静默失败，不影响游戏。
 */
public final class SoundPlayer {
    public enum Sound {
        MOVE(300, 30.0, 0.12),
        MERGE(620, 70.0, 0.20),
        WIN(880, 260.0, 0.18),
        OVER(150, 320.0, 0.16);

        private final double frequency;
        private final double millis;
        private final double volume;

        Sound(double frequency, double millis, double volume) {
            this.frequency = frequency;
            this.millis = millis;
            this.volume = volume;
        }
    }

    private static final float SAMPLE_RATE = 44100f;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(4),
            runnable -> {
                Thread thread = new Thread(runnable, "game2048-sound");
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private volatile boolean enabled = true;
    private volatile boolean deviceMissing;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void play(Sound sound) {
        if (!enabled || deviceMissing) {
            return;
        }
        executor.execute(() -> playNow(sound));
    }

    private void playNow(Sound sound) {
        byte[] pcm = synthesize(sound);
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(pcm, 0, pcm.length);
            line.drain();
        } catch (Exception e) {
            deviceMissing = true;
        }
    }

    private byte[] synthesize(Sound sound) {
        int samples = (int) (SAMPLE_RATE * sound.millis / 1000.0);
        byte[] pcm = new byte[samples * 2];
        double fadeSamples = Math.max(1.0, samples * 0.25);
        for (int i = 0; i < samples; i++) {
            double envelope = Math.min(1.0, i / fadeSamples) * Math.min(1.0, (samples - i) / fadeSamples);
            double wave = Math.sin(2 * Math.PI * sound.frequency * i / SAMPLE_RATE);
            short value = (short) (wave * envelope * sound.volume * Short.MAX_VALUE);
            pcm[i * 2] = (byte) (value & 0xFF);
            pcm[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
        }
        return pcm;
    }
}
