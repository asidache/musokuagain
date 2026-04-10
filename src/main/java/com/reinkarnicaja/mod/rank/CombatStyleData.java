package com.reinkarnicaja.mod.rank;

import com.reinkarnicaja.mod.character.CombatStyle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.EnumMap;
import java.util.Map;

/**
 * Данные боевого стиля: ранг и XP для каждого стиля
 */
public class CombatStyleData {
    private final Map<CombatStyle, Rank> ranks;
    private final Map<CombatStyle, Integer> xp;

    public CombatStyleData() {
        this.ranks = new EnumMap<>(CombatStyle.class);
        this.xp = new EnumMap<>(CombatStyle.class);
        
        for (CombatStyle style : CombatStyle.values()) {
            ranks.put(style, Rank.BEGINNER);
            xp.put(style, 0);
        }
    }

    public Rank getRank(CombatStyle style) {
        return ranks.getOrDefault(style, Rank.BEGINNER);
    }

    public int getXP(CombatStyle style) {
        return xp.getOrDefault(style, 0);
    }

    public void setRank(CombatStyle style, Rank rank) {
        ranks.put(style, rank);
    }

    public void setXP(CombatStyle style, int amount) {
        xp.put(style, Math.max(0, amount));
    }

    public void addXP(CombatStyle style, int amount) {
        int currentXP = getXp(style) + amount;
        xp.put(style, currentXP);
    }

    /**
     * Проверяет повышение ранга для стиля
     * @return true если ранг повысился
     */
    public boolean checkRankUp(CombatStyle style) {
        Rank currentRank = getRank(style);
        if (currentRank == Rank.DIVINE) {
            return false; // Максимальный ранг
        }

        int currentXP = getXp(style);
        int requiredXP = Rank.getRequiredXP(currentRank);

        if (currentXP >= requiredXP) {
            Rank newRank = Rank.fromLevel(currentRank.getLevel() + 1);
            setRank(style, newRank);
            setXP(style, currentXP - requiredXP); // Переносим избыток XP
            return true;
        }
        return false;
    }

    public static int getRequiredXP(Rank rank) {
        return Rank.getRequiredXP(rank);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        
        for (CombatStyle style : CombatStyle.values()) {
            String prefix = style.getKey() + "_";
            nbt.putString(prefix + "rank", getRank(style).getKey());
            nbt.putInt(prefix + "xp", getXp(style));
        }
        
        return nbt;
    }

    public void fromNbt(NbtCompound nbt) {
        for (CombatStyle style : CombatStyle.values()) {
            String prefix = style.getKey() + "_";
            if (nbt.contains(prefix + "rank", NbtElement.STRING_TYPE)) {
                String rankKey = nbt.getString(prefix + "rank");
                Rank rank = Rank.fromKey(rankKey);
                setRank(style, rank);
            }
            if (nbt.contains(prefix + "xp", NbtElement.INT_TYPE)) {
                setXP(style, nbt.getInt(prefix + "xp"));
            }
        }
    }
}
