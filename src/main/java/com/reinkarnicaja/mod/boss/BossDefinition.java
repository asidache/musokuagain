package com.reinkarnicaja.mod.boss;

import com.reinkarnicaja.mod.rank.Rank;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

/**
 * Enum всех 7 боссов вселенной Mushoku Tensei
 */
public enum BossDefinition {
    GOLDEN_MAGE(
        "golden_mage",
        Rank.INTERMEDIATE,
        EntityType.EVOKER,
        80.0f,
        6.0f,
        0.5f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/golden_mage.png")
    ),
    MAGIC_KNIGHT(
        "magic_knight",
        Rank.ADVANCED,
        EntityType.VINDICATOR,
        120.0f,
        8.0f,
        0.8f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/magic_knight.png")
    ),
    EARTH_SERPENT(
        "earth_serpent",
        Rank.ADVANCED,
        EntityType.RAVAGER,
        150.0f,
        10.0f,
        1.0f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/earth_serpent.png")
    ),
    HYDRA(
        "hydra",
        Rank.SAINT,
        EntityType.ELDER_GUARDIAN,
        200.0f,
        12.0f,
        1.2f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/hydra.png")
    ),
    HOLY_SWORD_DEMON(
        "holy_sword_demon",
        Rank.KING,
        EntityType.IRON_GOLEM,
        280.0f,
        14.0f,
        1.5f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/holy_sword_demon.png")
    ),
    ANCIENT_DRAGON(
        "ancient_dragon",
        Rank.EMPEROR,
        EntityType.ENDER_DRAGON,
        500.0f,
        18.0f,
        2.0f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/ancient_dragon.png")
    ),
    DEMON_LORD_LAPLACE(
        "demon_lord_laplace",
        Rank.DIVINE,
        EntityType.WITHER,
        800.0f,
        25.0f,
        3.0f,
        Identifier.of("reinkarnicaja_mod", "textures/entity/bosses/demon_lord_laplace.png")
    );

    private final String key;
    private final Rank requiredRank;
    private final EntityType<?> entityType;
    private final float maxHealth;
    private final float attackDamage;
    private final float healthRegenPerSec;
    private final Identifier texture;

    BossDefinition(String key, Rank requiredRank, EntityType<?> entityType, 
                   float maxHealth, float attackDamage, float healthRegenPerSec,
                   Identifier texture) {
        this.key = key;
        this.requiredRank = requiredRank;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.healthRegenPerSec = healthRegenPerSec;
        this.texture = texture;
    }

    public String getKey() {
        return key;
    }

    public Rank getRequiredRank() {
        return requiredRank;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public float getHealthRegenPerSec() {
        return healthRegenPerSec;
    }

    public Identifier getTexture() {
        return texture;
    }

    /**
     * Получить босса по ключу
     */
    public static BossDefinition fromKey(String key) {
        for (BossDefinition def : values()) {
            if (def.key.equalsIgnoreCase(key)) {
                return def;
            }
        }
        return GOLDEN_MAGE; // default
    }

    /**
     * Проверить соответствует ли ранг игрока боссу
     */
    public boolean isRankAppropriate(Rank playerRank) {
        return playerRank.getLevel() >= requiredRank.getLevel();
    }
}
