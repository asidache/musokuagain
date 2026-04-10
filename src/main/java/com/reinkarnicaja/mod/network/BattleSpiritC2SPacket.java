package com.reinkarnicaja.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Клиент -> Сервер: Включить/выключить Боевой Дух.
 */
public record BattleSpiritC2SPacket(boolean activate) implements CustomPayload {
    public static final Id<BattleSpiritC2SPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "battle_spirit_c2s"));
    public static final PacketCodec<RegistryByteBuf, BattleSpiritC2SPacket> CODEC = PacketCodec.of(
            (buf, packet) -> buf.writeBoolean(packet.activate),
            buf -> new BattleSpiritC2SPacket(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
