package com.reinkarnicaja.mod.rank;

/**
 * Уровни рангов персонажа
 */
public enum Rank {
    BEGINNER(0, "beginner"),
    INTERMEDIATE(1, "intermediate"),
    ADVANCED(2, "advanced"),
    SAINT(3, "saint"),
    KING(4, "king"),
    EMPEROR(5, "emperor"),
    DIVINE(6, "divine");

    private final int level;
    private final String key;

    Rank(int level, String key) {
        this.level = level;
        this.key = key;
    }

    public int getLevel() {
        return level;
    }

    public String getKey() {
        return key;
    }

    /**
     * Возвращает необходимый XP для следующего ранга
     */
    public static int getRequiredXP(Rank rank) {
        return switch (rank) {
            case BEGINNER -> 100;
            case INTERMEDIATE -> 300;
            case ADVANCED -> 700;
            case SAINT -> 1500;
            case KING -> 3000;
            case EMPEROR -> 6000;
            case DIVINE -> 10000;
        };
    }

    /**
     * Возвращает ранг по уровню
     */
    public static Rank fromLevel(int level) {
        for (Rank rank : values()) {
            if (rank.level == level) {
                return rank;
            }
        }
        return BEGINNER;
    }

    /**
     * Возвращает ранг по ключу
     */
    public static Rank fromKey(String key) {
        for (Rank rank : values()) {
            if (rank.key.equalsIgnoreCase(key)) {
                return rank;
            }
        }
        return BEGINNER;
    }
}
