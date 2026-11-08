package game2048.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** 配色与字体，明亮/暗黑两套主题共用同一套绘制代码。 */
public final class Theme {
    public enum Mode { LIGHT, DARK }

    private static final String[] FONT_CANDIDATES = {"微软雅黑", "Microsoft YaHei", "黑体", "SimHei"};
    private static final String FONT_FAMILY = pickFontFamily();

    private static Mode mode = Mode.LIGHT;

    private Theme() {
    }

    public static Mode mode() {
        return mode;
    }

    public static void setMode(Mode next) {
        mode = next;
    }

    public static void toggle() {
        mode = mode == Mode.LIGHT ? Mode.DARK : Mode.LIGHT;
    }

    public static Font uiFont(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    private static String pickFontFamily() {
        Set<String> available = new HashSet<String>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String candidate : FONT_CANDIDATES) {
            if (available.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Color background() {
        return mode == Mode.LIGHT ? new Color(0xFAF8EF) : new Color(0x17171B);
    }

    public static Color boardBackground() {
        return mode == Mode.LIGHT ? new Color(0xBBADA0) : new Color(0x2A2A32);
    }

    public static Color emptyCell() {
        return mode == Mode.LIGHT ? new Color(0xCDC1B4) : new Color(0x35353F);
    }

    public static Color scoreBar() {
        return mode == Mode.LIGHT ? new Color(0xBBADA0) : new Color(0x232329);
    }

    public static Color scoreText() {
        return mode == Mode.LIGHT ? Color.WHITE : new Color(0xF5F5F5);
    }

    public static Color bestText() {
        return mode == Mode.LIGHT ? new Color(0xEEE4DA) : new Color(0xB8B8C2);
    }

    public static Color overlayText() {
        return mode == Mode.LIGHT ? new Color(0x776E65) : new Color(0xF0F0F0);
    }

    public static Color overlayWash() {
        return mode == Mode.LIGHT ? new Color(0xFA, 0xF8, 0xEF, 200) : new Color(0x12, 0x12, 0x16, 205);
    }

    public static Color buttonFill() {
        return mode == Mode.LIGHT ? new Color(0x8F7A66) : new Color(0x4A4A58);
    }

    public static Color buttonHover() {
        return mode == Mode.LIGHT ? new Color(0xA08A78) : new Color(0x5A5A6A);
    }

    public static Color buttonPressed() {
        return mode == Mode.LIGHT ? new Color(0x786554) : new Color(0x3A3A46);
    }

    public static Color buttonText() {
        return mode == Mode.LIGHT ? Color.WHITE : new Color(0xF2F2F5);
    }

    public static Color hint() {
        return mode == Mode.LIGHT ? new Color(0xE8B923) : new Color(0xF2C94C);
    }

    public static Color tileColor(int value) {
        switch (value) {
            case 2:
                return mode == Mode.LIGHT ? new Color(0xEEE4DA) : new Color(0x3A3A44);
            case 4:
                return mode == Mode.LIGHT ? new Color(0xEDE0C8) : new Color(0x43434F);
            case 8:
                return mode == Mode.LIGHT ? new Color(0xF2B179) : new Color(0xB06A45);
            case 16:
                return mode == Mode.LIGHT ? new Color(0xF59563) : new Color(0xC2703F);
            case 32:
                return mode == Mode.LIGHT ? new Color(0xF67C5F) : new Color(0xCE6A3C);
            case 64:
                return mode == Mode.LIGHT ? new Color(0xF65E3B) : new Color(0xD8572F);
            case 128:
                return mode == Mode.LIGHT ? new Color(0xEDCF72) : new Color(0xC9A23A);
            case 256:
                return mode == Mode.LIGHT ? new Color(0xEDCC61) : new Color(0xC99F2E);
            case 512:
                return mode == Mode.LIGHT ? new Color(0xEDC850) : new Color(0xC79A22);
            case 1024:
                return mode == Mode.LIGHT ? new Color(0xEDC53F) : new Color(0xC29418);
            case 2048:
                return mode == Mode.LIGHT ? new Color(0xEDC22E) : new Color(0xBC8E0E);
            case 4096:
                return mode == Mode.LIGHT ? new Color(0x3C3A32) : new Color(0x6E6A5E);
            default:
                return mode == Mode.LIGHT ? new Color(0x2E2B26) : new Color(0x55524A);
        }
    }

    public static Color tileTextColor(int value) {
        if (value <= 4) {
            return mode == Mode.LIGHT ? new Color(0x776E65) : new Color(0xE8E2D8);
        }
        return Color.WHITE;
    }
}
