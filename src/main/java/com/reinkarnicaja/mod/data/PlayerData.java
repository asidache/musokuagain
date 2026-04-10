package com.reinkarnicaja.mod.data;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.character.CombatStyle;
import com.reinkarnicaja.mod.rank.CombatStyleData;
import com.reinkarnicaja.mod.rank.Rank;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Центральное хранилище данных игрока
 */
public class PlayerData {
    private static final Map<UUID, PlayerData> PLAYER_DATA_MAP = new HashMap<>();

    // Основная информация
    private CharacterDefinition character;
    private CombatStyle activeStyle;
    private int totalXP;

    // Мана
    private float currentMana;
    private float maxMana;
    private float manaRegenRate;
    private int manaTrainingXP;
    private boolean isInDepletion;

    // Данные стилей
    private CombatStyleData styleData;

    // Специфичные механики
    private int calmanStationaryTicks;
    private int debugRank;

    // Dash состояния (для Эрис)
    private int dashCooldown;
    private boolean hasPostDashBonus;
    private boolean recentlyDashing;
    private int dashReleaseTicks;

    // Лаплас: последняя использованная стихия
    @Nullable
    private CombatStyle lastSpellStyle;
    private boolean elementSwapBonus;

    // Рудеус: двойной каст
    private boolean dualCastActive;

    // Выбранное заклинание
    private String selectedSpellId;
    private long spellSelectTime;

    // Броня Бога Битвы (Бадигади)
    private boolean battleArmorCanRemove;
    private float battleArmorDrainRate;

    // Бинды заклинаний (слоты 1-9)
    private final Map<Integer, String> spellBinds;

    // Боевой дух
    private boolean battleSpiritActive;

    private PlayerData() {
        this.character = null;
        this.activeStyle = CombatStyle.MAGIC;
        this.totalXP = 0;
        this.currentMana = 100f;
        this.maxMana = 100f;
        this.manaRegenRate = 2f;
        this.manaTrainingXP = 0;
        this.isInDepletion = false;
        this.styleData = new CombatStyleData();
        this.calmanStationaryTicks = 0;
        this.debugRank = 0;
        this.dashCooldown = 0;
        this.hasPostDashBonus = false;
        this.recentlyDashing = false;
        this.dashReleaseTicks = 0;
        this.lastSpellStyle = null;
        this.elementSwapBonus = false;
        this.dualCastActive = false;
        this.selectedSpellId = "";
        this.spellSelectTime = 0;
        this.battleArmorCanRemove = false;
        this.battleArmorDrainRate = 0.001f; // 0.1% в секунду
        this.spellBinds = new HashMap<>();
        this.battleSpiritActive = false;
    }

    public static PlayerData get(PlayerEntity player) {
        UUID uuid = player.getUuid();
        return PLAYER_DATA_MAP.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public static void remove(UUID uuid) {
        PLAYER_DATA_MAP.remove(uuid);
    }

    public static void clearAll() {
        PLAYER_DATA_MAP.clear();
    }

    // === Геттеры и сеттеры ===

    public CharacterDefinition getCharacter() {
        return character;
    }

    public void setCharacter(CharacterDefinition character) {
        this.character = character;
        if (character != null) {
            this.maxMana = character.canUseMagic() ? 100f : 50f;
            this.currentMana = this.maxMana;
        }
    }

    public CombatStyle getActiveStyle() {
        return activeStyle;
    }

    public void setActiveStyle(CombatStyle style) {
        this.activeStyle = style;
    }

    public int getTotalXP() {
        return totalXP;
    }

    public void addTotalXP(int amount) {
        this.totalXP += amount;
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public float getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(float maxMana) {
        this.maxMana = maxMana;
    }

    public float getManaRegenRate() {
        return manaRegenRate;
    }

    public void setManaRegenRate(float rate) {
        this.manaRegenRate = rate;
    }

    public int getManaTrainingXP() {
        return manaTrainingXP;
    }

    public void addManaTrainingXP(int amount) {
        this.manaTrainingXP += amount;
    }

    public boolean isInDepletion() {
        return isInDepletion;
    }

    public void setInDepletion(boolean depletion) {
        isInDepletion = depletion;
    }

    public CombatStyleData getStyleData() {
        return styleData;
    }

    public int getCalmanStationaryTicks() {
        return calmanStationaryTicks;
    }

    public void setCalmanStationaryTicks(int ticks) {
        this.calmanStationaryTicks = ticks;
    }

    public int getDebugRank() {
        return debugRank;
    }

    public void setDebugRank(int rank) {
        this.debugRank = rank;
    }

    // === Dash методы ===

    public int getDashCooldown() {
        return dashCooldown;
    }

    public void setDashCooldown(int cooldown) {
        this.dashCooldown = cooldown;
    }

    public boolean hasPostDashDamageBonus() {
        return hasPostDashBonus;
    }

    public void setPostDashBonus(boolean bonus) {
        this.hasPostDashBonus = bonus;
    }

    public boolean isRecentlyDashing() {
        return recentlyDashing;
    }

    public void setRecentlyDashing(boolean dashing) {
        this.recentlyDashing = dashing;
    }

    public int getDashReleaseTicks() {
        return dashReleaseTicks;
    }

    public void setDashReleaseTicks(int ticks) {
        this.dashReleaseTicks = ticks;
    }

    // === Лаплас методы ===

    @Nullable
    public CombatStyle getLastSpellStyle() {
        return lastSpellStyle;
    }

    public void setLastSpellStyle(@Nullable CombatStyle style) {
        this.lastSpellStyle = style;
    }

    public boolean hasElementSwapBonus() {
        return elementSwapBonus;
    }

    public void setElementSwapBonus(boolean bonus) {
        this.elementSwapBonus = bonus;
    }

    // === Рудеус методы ===

    public boolean isDualCastActive() {
        return dualCastActive;
    }

    public void setDualCastActive(boolean active) {
        this.dualCastActive = active;
    }

    // === Заклинания ===

    public String getSelectedSpellId() {
        return selectedSpellId;
    }

    public void setSelectedSpellId(String id) {
        this.selectedSpellId = id;
    }

    public long getSpellSelectTime() {
        return spellSelectTime;
    }

    public void setSpellSelectTime(long time) {
        this.spellSelectTime = time;
    }

    // === Броня Бога Битвы ===

    public boolean isBattleArmorCanRemove() {
        return battleArmorCanRemove;
    }

    public void setBattleArmorCanRemove(boolean canRemove) {
        this.battleArmorCanRemove = canRemove;
    }

    public float getBattleArmorDrainRate() {
        return battleArmorDrainRate;
    }

    public void setBattleArmorDrainRate(float rate) {
        this.battleArmorDrainRate = rate;
    }

    // === Бинды заклинаний ===

    public Map<Integer, String> getSpellBinds() {
        return spellBinds;
    }

    public String getSpellBind(int slotIndex) {
        return spellBinds.get(slotIndex);
    }

    public void setSpellBind(int slotIndex, String spellId) {
        if (spellId == null || spellId.isEmpty()) {
            spellBinds.remove(slotIndex);
        } else {
            spellBinds.put(slotIndex, spellId);
        }
    }

    public void clearSpellBinds() {
        spellBinds.clear();
    }

    // === Боевой дух ===

    public boolean isBattleSpiritActive() {
        return battleSpiritActive;
    }

    public void setBattleSpiritActive(boolean active) {
        this.battleSpiritActive = active;
    }

    // === Мана операции ===

    /**
     * Тратит ману. Возвращает true если успешно.
     */
    public boolean spendMana(float amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            if (currentMana <= 0) {
                currentMana = 0;
                isInDepletion = true;
            }
            return true;
        }
        return false;
    }

    /**
     * Регенерирует ману
     */
    public void regenMana(float amount) {
        if (currentMana < maxMana) {
            currentMana = Math.min(maxMana, currentMana + amount);
            if (currentMana > 0) {
                isInDepletion = false;
            }
        }
    }

    // === Проверки рангов ===

    /**
     * Возвращает эффективный ранг (с учётом дебаг режима)
     */
    public Rank getEffectiveRank(CombatStyle style) {
        if (debugRank > 0) {
            return Rank.fromLevel(Math.min(debugRank, 6));
        }
        return styleData.getRank(style);
    }

    /**
     * Проверяет бонус за использование разных стилей
     */
    public boolean hasCrossStyleBoost(CombatStyle style) {
        // Простая реализация: если есть прогресс в других стилях
        for (CombatStyle other : CombatStyle.values()) {
            if (other != style && styleData.getRank(other).getLevel() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Может ли игрок переключить стиль
     */
    public boolean canSwitchStyle(CombatStyle newStyle) {
        // Орстед не может использовать магию
        if (character == CharacterDefinition.ORSTED && newStyle == CombatStyle.MAGIC) {
            return false;
        }
        // Бадигади ограничен INTERMEDIATE для магии
        if (character == CharacterDefinition.BADIGADI && newStyle == CombatStyle.MAGIC) {
            Rank rank = styleData.getRank(newStyle);
            return rank.getLevel() <= 1; // INTERMEDIATE = 1
        }
        return true;
    }

    /**
     * Проверка на Императорский ранг
     */
    public boolean hasImperialRank() {
        return styleData.getRank(activeStyle) == Rank.EMPEROR;
    }

    /**
     * Возвращает все ранги в текстовом виде
     */
    public Text getAllRanks() {
        StringBuilder sb = new StringBuilder();
        for (CombatStyle style : CombatStyle.values()) {
            Rank rank = styleData.getRank(style);
            sb.append(style.getDisplayName().getString())
              .append(": ")
              .append(rank.getDisplayName().getString())
              .append("\n");
        }
        return Text.literal(sb.toString());
    }

    // === NBT сериализация ===

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        if (character != null) {
            nbt.putString("character", character.getKey());
        }
        nbt.putString("activeStyle", activeStyle.getKey());
        nbt.putInt("totalXP", totalXP);
        nbt.putFloat("currentMana", currentMana);
        nbt.putFloat("maxMana", maxMana);
        nbt.putFloat("manaRegenRate", manaRegenRate);
        nbt.putInt("manaTrainingXP", manaTrainingXP);
        nbt.putBoolean("isInDepletion", isInDepletion);
        nbt.put("styleData", styleData.toNbt());
        nbt.putInt("calmanStationaryTicks", calmanStationaryTicks);
        nbt.putInt("debugRank", debugRank);
        nbt.putInt("dashCooldown", dashCooldown);
        nbt.putBoolean("hasPostDashBonus", hasPostDashBonus);
        nbt.putBoolean("recentlyDashing", recentlyDashing);
        nbt.putInt("dashReleaseTicks", dashReleaseTicks);
        nbt.putBoolean("dualCastActive", dualCastActive);
        nbt.putString("selectedSpellId", selectedSpellId);
        nbt.putLong("spellSelectTime", spellSelectTime);
        nbt.putBoolean("battleArmorCanRemove", battleArmorCanRemove);
        nbt.putFloat("battleArmorDrainRate", battleArmorDrainRate);

        if (lastSpellStyle != null) {
            nbt.putString("lastSpellStyle", lastSpellStyle.getKey());
        }
        nbt.putBoolean("elementSwapBonus", elementSwapBonus);

        // Сериализация биндов заклинаний
        NbtCompound bindsNbt = new NbtCompound();
        for (Map.Entry<Integer, String> entry : spellBinds.entrySet()) {
            bindsNbt.putString("slot_" + entry.getKey(), entry.getValue());
        }
        nbt.put("spellBinds", bindsNbt);

        nbt.putBoolean("battleSpiritActive", battleSpiritActive);

        return nbt;
    }

    public void fromNbt(NbtCompound nbt) {
        if (nbt.contains("character")) {
            character = CharacterDefinition.fromKey(nbt.getString("character"));
        }
        if (nbt.contains("activeStyle")) {
            activeStyle = CombatStyle.fromKey(nbt.getString("activeStyle"));
        }
        totalXP = nbt.getInt("totalXP");
        currentMana = nbt.getFloat("currentMana");
        maxMana = nbt.getFloat("maxMana");
        manaRegenRate = nbt.getFloat("manaRegenRate");
        manaTrainingXP = nbt.getInt("manaTrainingXP");
        isInDepletion = nbt.getBoolean("isInDepletion");
        if (nbt.contains("styleData")) {
            styleData.fromNbt(nbt.getCompound("styleData"));
        }
        calmanStationaryTicks = nbt.getInt("calmanStationaryTicks");
        debugRank = nbt.getInt("debugRank");
        dashCooldown = nbt.getInt("dashCooldown");
        hasPostDashBonus = nbt.getBoolean("hasPostDashBonus");
        recentlyDashing = nbt.getBoolean("recentlyDashing");
        dashReleaseTicks = nbt.getInt("dashReleaseTicks");
        dualCastActive = nbt.getBoolean("dualCastActive");
        selectedSpellId = nbt.getString("selectedSpellId");
        spellSelectTime = nbt.getLong("spellSelectTime");
        battleArmorCanRemove = nbt.getBoolean("battleArmorCanRemove");
        battleArmorDrainRate = nbt.getFloat("battleArmorDrainRate");

        if (nbt.contains("lastSpellStyle")) {
            lastSpellStyle = CombatStyle.fromKey(nbt.getString("lastSpellStyle"));
        }
        elementSwapBonus = nbt.getBoolean("elementSwapBonus");

        // Десериализация биндов заклинаний
        spellBinds.clear();
        if (nbt.contains("spellBinds")) {
            NbtCompound bindsNbt = nbt.getCompound("spellBinds");
            for (String key : bindsNbt.getKeys()) {
                if (key.startsWith("slot_")) {
                    int slotIndex = Integer.parseInt(key.substring(5));
                    String spellId = bindsNbt.getString(key);
                    spellBinds.put(slotIndex, spellId);
                }
            }
        }

        battleSpiritActive = nbt.getBoolean("battleSpiritActive");
    }

    /**
     * Синхронизация данных с клиентом
     */
    public void syncData(ServerPlayerEntity player) {
        // Отправка пакета PlayerDataSyncS2CPacket
        net.minecraft.server.network.ServerPlayerEntity spe = (net.minecraft.server.network.ServerPlayerEntity) player;
        var packet = new com.reinkarnicaja.mod.network.PlayerDataSyncS2CPacket(
            character != null ? character.getKey() : "",
            activeStyle != null ? activeStyle.getKey() : "",
            totalXP,
            currentMana,
            maxMana,
            manaRegenRate,
            isInDepletion,
            calmanStationaryTicks,
            debugRank,
            dashCooldown,
            hasPostDashBonus,
            recentlyDashing,
            dashReleaseTicks,
            selectedSpellId,
            spellSelectTime,
            battleSpiritActive,
            dualCastActive,
            spellBinds
        );
        
        if (spe.networkHandler != null) {
            spe.networkHandler.sendPacket(packet);
        }
    }
}
