package com.reinkarnicaja.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Клиент -> Сервер: Игрок выбрал заклинание в радиальном меню.
 */
public record SpellSelectC2SPacket(int spellId) implements CustomPayload {
    public static final Id<SpellSelectC2SPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "spell_select_c2s"));
    public static final PacketCodec<RegistryByteBuf, SpellSelectC2SPacket> CODEC = PacketCodec.of(
            (buf, packet) -> buf.writeInt(packet.spellId),
            buf -> new SpellSelectC2SPacket(buf.readInt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
