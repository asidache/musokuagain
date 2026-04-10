package com.reinkarnicaja.mod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import com.reinkarnicaja.mod.ReincarnationMod;
import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.CharacterAssignmentManager;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.mechanics.BattleSpiritManager;
import com.reinkarnicaja.mod.spell.SpellManager;

/**
 * Регистрация всех сетевых пакетов и их обработчиков.
 */
public class ModPacketHandler {

    public static void registerS2CPackets() {
        // Сервер -> Клиент: выбор персонажа
        PayloadTypeRegistry.playS2C().register(CharacterSelectS2CPacket.ID, CharacterSelectS2CPacket.CODEC);
        
        // Сервер -> Клиент: синхронизация данных
        PayloadTypeRegistry.playS2C().register(PlayerDataSyncS2CPacket.ID, PlayerDataSyncS2CPacket.CODEC);
    }

    public static void registerC2SPackets() {
        // Клиент -> Сервер: выбор персонажа
        PayloadTypeRegistry.playC2S().register(CharacterSelectC2SPacket.ID, CharacterSelectC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CharacterSelectC2SPacket.ID, (packet, context) -> {
            handleCharacterSelect(packet, context.player());
        });

        // Клиент -> Сервер: выбор заклинания
        PayloadTypeRegistry.playC2S().register(SpellSelectC2SPacket.ID, SpellSelectC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpellSelectC2SPacket.ID, (packet, context) -> {
            handleSpellSelect(packet, context.player());
        });

        // Клиент -> Сервер: бинд заклинания
        PayloadTypeRegistry.playC2S().register(SpellBindC2SPacket.ID, SpellBindC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpellBindC2SPacket.ID, (packet, context) -> {
            handleSpellBind(packet, context.player());
        });

        // Клиент -> Сервер: каст заклинания
        PayloadTypeRegistry.playC2S().register(SpellCastC2SPacket.ID, SpellCastC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpellCastC2SPacket.ID, (packet, context) -> {
            handleSpellCast(packet, context.player());
        });

        // Клиент -> Сервер: боевой дух
        PayloadTypeRegistry.playC2S().register(BattleSpiritC2SPacket.ID, BattleSpiritC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BattleSpiritC2SPacket.ID, (packet, context) -> {
            handleBattleSpirit(packet, context.player());
        });
    }

    private static void handleCharacterSelect(CharacterSelectC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || !server.isOnThread()) {
            server.execute(() -> handleCharacterSelect(packet, player));
            return;
        }

        String playerName = player.getName().getString();
        CharacterDefinition character = packet.getCharacter();

        if (character == null) {
            ReincarnationMod.LOGGER.warn("Игрок {} попытался выбрать несуществующего персонажа", playerName);
            return;
        }

        if (CharacterAssignmentManager.hasAssignment(playerName)) {
            ReincarnationMod.LOGGER.warn("Игрок {} уже имеет назначенного персонажа", playerName);
            return;
        }

        if (CharacterAssignmentManager.isCharacterTaken(character)) {
            ReincarnationMod.LOGGER.warn("Персонаж {} уже занят, отказ игроку {}", character.name(), playerName);
            // TODO: отправить пакет об ошибке клиенту
            return;
        }

        // Назначить персонажа
        CharacterAssignmentManager.assign(playerName, character);
        
        // Инициализировать PlayerData
        PlayerData data = PlayerData.get(player);
        data.setCharacter(character);
        data.setActiveStyle(character.getDefaultStyle());
        
        // Синхронизировать с клиентом
        PlayerData.syncData(data, player);
        
        ReincarnationMod.LOGGER.info("Игрок {} выбрал персонажа {}", playerName, character.name());
    }

    private static void handleSpellSelect(SpellSelectC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || !server.isOnThread()) {
            server.execute(() -> handleSpellSelect(packet, player));
            return;
        }

        PlayerData data = PlayerData.get(player);
        data.setSelectedSpellId(String.valueOf(packet.spellId()));
        data.setSpellSelectTime(System.currentTimeMillis());
        
        PlayerData.syncData(data, player);
    }

    private static void handleSpellBind(SpellBindC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || !server.isOnThread()) {
            server.execute(() -> handleSpellBind(packet, player));
            return;
        }

        PlayerData data = PlayerData.get(player);
        data.setSpellBind(packet.slotIndex(), packet.spellId());
        
        // Синхронизировать с клиентом
        PlayerData.syncData(data, player);
        
        ReincarnationMod.LOGGER.info("Игрок {} привязал заклинание {} к слоту {}", 
            player.getName().getString(), packet.spellId(), packet.slotIndex() + 1);
    }

    private static void handleSpellCast(SpellCastC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || !server.isOnThread()) {
            server.execute(() -> handleSpellCast(packet, player));
            return;
        }

        int spellId = packet.spellId();
        float chargeSeconds = packet.chargeSeconds();
        
        SpellManager.castSpell(player, spellId, chargeSeconds);
    }

    private static void handleBattleSpirit(BattleSpiritC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || !server.isOnThread()) {
            server.execute(() -> handleBattleSpirit(packet, player));
            return;
        }

        boolean activate = packet.activate();
        BattleSpiritManager.toggleBattleSpirit(player, activate);
    }

    /**
     * Отправить пакет выбора персонажа игроку (если у него нет назначения).
     */
    public static void sendCharacterSelectPacket(ServerPlayerEntity player) {
        if (!CharacterAssignmentManager.hasAssignment(player.getName().getString())) {
            ClientPlayNetworking.send(new CharacterSelectS2CPacket());
        }
    }

    /**
     * Отправить синхронизацию данных игроку.
     */
    public static void syncPlayerData(ServerPlayerEntity player) {
        PlayerData data = PlayerData.get(player);
        PlayerData.syncData(data, player);
    }
}
