package com.reinkarnicaja.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Клиент -> Сервер: Каст заклинания (с временем зарядки для заряжаемых заклинаний).
 */
public record SpellCastC2SPacket(int spellId, float chargeSeconds) implements CustomPayload {
    public static final Id<SpellCastC2SPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "spell_cast_c2s"));
    public static final PacketCodec<RegistryByteBuf, SpellCastC2SPacket> CODEC = PacketCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.spellId);
                buf.writeFloat(packet.chargeSeconds);
            },
            buf -> new SpellCastC2SPacket(buf.readInt(), buf.readFloat())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
