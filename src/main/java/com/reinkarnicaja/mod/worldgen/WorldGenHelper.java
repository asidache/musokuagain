package com.reinkarnicaja.mod.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.spawner.StructureSpawner;

import com.reinkarnicaja.mod.ReincarnationMod;

/**
 * Генерация структур мира: Battle God Vault на дне океана.
 */
public class WorldGenHelper {

    // Ключ структуры Battle God Vault
    public static final RegistryKey<net.minecraft.world.gen.structure.Structure> BATTLE_GOD_VAULT_KEY =
            RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.of("reinkarnicaja_mod", "battle_god_vault"));

    /**
     * Регистрация генерации структур.
     */
    public static void registerWorldGen() {
        ReincarnationMod.LOGGER.info("Регистрация генерации структур Reinkarnicaja Mod");
        
        // Спавн Battle God Vault только в глубоких океанах
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld().and(BiomeSelectors.includeByTag(net.minecraft.world.biome.BiomeTags.IS_OCEAN)),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                BATTLE_GOD_VAULT_KEY
        );
        
        ReincarnationMod.LOGGER.info("Battle God Vault будет генерироваться в глубоких океанах");
    }

    /**
     * Получить идентификатор структуры Battle God Vault.
     */
    public static Identifier getBattleGodVaultId() {
        return Identifier.of("reinkarnicaja_mod", "battle_god_vault");
    }
}
