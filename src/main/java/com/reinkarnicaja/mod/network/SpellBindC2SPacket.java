package com.reinkarnicaja.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Клиент -> Сервер: Игрок привязывает заклинание к слоту (1-9).
 */
public record SpellBindC2SPacket(int slotIndex, String spellId) implements CustomPayload {
    public static final Id<SpellBindC2SPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "spell_bind_c2s"));
    public static final PacketCodec<RegistryByteBuf, SpellBindC2SPacket> CODEC = PacketCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.slotIndex);
                buf.writeString(packet.spellId);
            },
            buf -> {
                int slotIndex = buf.readInt();
                String spellId = buf.readString();
                return new SpellBindC2SPacket(slotIndex, spellId);
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
