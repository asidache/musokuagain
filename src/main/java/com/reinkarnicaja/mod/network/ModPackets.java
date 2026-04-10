package com.reinkarnicaja.mod.network;

import com.reinkarnicaja.mod.ReincarnationMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Регистрация сетевых пакетов мода
 */
public class ModPackets {
    public static final Identifier CHARACTER_SELECT_C2S_ID = Identifier.of(ReincarnationMod.MOD_ID, "character_select_c2s");
    public static final Identifier CHARACTER_SELECT_S2C_ID = Identifier.of(ReincarnationMod.MOD_ID, "character_select_s2c");
    public static final Identifier PLAYER_DATA_SYNC_S2C_ID = Identifier.of(ReincarnationMod.MOD_ID, "player_data_sync_s2c");
    public static final Identifier SPELL_CAST_C2S_ID = Identifier.of(ReincarnationMod.MOD_ID, "spell_cast_c2s");
    public static final Identifier SPELL_SELECT_C2S_ID = Identifier.of(ReincarnationMod.MOD_ID, "spell_select_c2s");
    public static final Identifier BATTLE_SPIRIT_C2S_ID = Identifier.of(ReincarnationMod.MOD_ID, "battle_spirit_c2s");

    public static void register() {
        // Регистрация типов пакетов будет реализована в отдельных классах
        // Здесь только заготовка для будущей реализации
    }

    /**
     * Отправить пакет клиенту
     */
    public static void sendToClient(CustomPayload packet, net.minecraft.server.network.ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, packet);
    }

    /**
     * Отправить пакет серверу
     */
    public static void sendToServer(CustomPayload packet) {
        ClientPlayNetworking.send(packet);
    }
}
