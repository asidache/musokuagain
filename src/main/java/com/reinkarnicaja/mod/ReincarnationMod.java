package com.reinkarnicaja.mod;

import com.reinkarnicaja.mod.commands.ModCommands;
import com.reinkarnicaja.mod.data.CharacterAssignmentManager;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.equipment.EquipmentHandler;
import com.reinkarnicaja.mod.mana.ManaManager;
import com.reinkarnicaja.mod.mechanics.BattleSpiritManager;
import com.reinkarnicaja.mod.mechanics.DashMechanic;
import com.reinkarnicaja.mod.network.ModPacketHandler;
import com.reinkarnicaja.mod.passive.PassiveHandler;
import com.reinkarnicaja.mod.rank.RankManager;
import com.reinkarnicaja.mod.boss.BossSpawner;
import com.reinkarnicaja.mod.worldgen.WorldGenHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReincarnationMod implements ModInitializer {
    public static final String MOD_ID = "reinkarnicaja_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Reincarnation Mod v1.0.0");

        // Регистрация пакетов сети
        ModPacketHandler.registerS2CPackets();
        ModPacketHandler.registerC2SPackets();

        // Регистрация предметов
        EquipmentHandler.registerItems();

        // Регистрация обработчиков
        PassiveHandler.register();
        ManaManager.register();
        RankManager.register();
        BattleSpiritManager.register();
        BossSpawner.register();
        DashMechanic.register();
        
        // Регистрация генерации мира
        WorldGenHelper.registerWorldGen();

        // Регистрация команд
        CommandRegistrationCallback.EVENT.register(ModCommands::register);

        // Загрузка CharacterAssignmentManager при старте сервера
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        LOGGER.info("Reincarnation Mod initialized successfully");
    }

    private void onServerStarting(MinecraftServer server) {
        ReincarnationMod.server = server;
        CharacterAssignmentManager.load(server.getSavePath().toFile());
        LOGGER.info("Character assignments loaded");
    }

    private void onServerStopping(MinecraftServer server) {
        CharacterAssignmentManager.save();
        PlayerData.clearAll();
        LOGGER.info("Reincarnation Mod stopped");
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
