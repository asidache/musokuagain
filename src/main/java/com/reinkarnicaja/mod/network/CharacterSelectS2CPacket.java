package com.reinkarnicaja.mod.network;

import com.reinkarnicaja.mod.data.CharacterAssignmentManager;
import com.reinkarnicaja.mod.data.PlayerData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Сервер -> Клиент: Открывает экран выбора персонажа, если у игрока нет назначения.
 */
public record CharacterSelectS2CPacket() implements CustomPayload {
    public static final Id<CharacterSelectS2CPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "character_select_s2c"));
    public static final PacketCodec<RegistryByteBuf, CharacterSelectS2CPacket> CODEC = PacketCodec.unit(new CharacterSelectS2CPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
