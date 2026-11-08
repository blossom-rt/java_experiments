package game2048.model;

/** 难度：只影响新方块出现 4 的概率，规则其余部分保持一致。 */
public enum Difficulty {
    EASY("简单", 0.10),
    NORMAL("标准", 0.25),
    HARD("困难", 0.50);

    private final String label;
    private final double fourChance;

    Difficulty(String label, double fourChance) {
        this.label = label;
        this.fourChance = fourChance;
    }

    public String label() {
        return label;
    }

    public double fourChance() {
        return fourChance;
    }

    public Difficulty next() {
        Difficulty[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
