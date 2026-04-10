package com.reinkarnicaja.mod.network;

import com.reinkarnicaja.mod.character.CharacterDefinition;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Клиент -> Сервер: Игрок выбрал персонажа.
 */
public record CharacterSelectC2SPacket(String characterKey) implements CustomPayload {
    public static final Id<CharacterSelectC2SPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "character_select_c2s"));
    public static final PacketCodec<RegistryByteBuf, CharacterSelectC2SPacket> CODEC = PacketCodec.of(
            (buf, packet) -> buf.writeString(packet.characterKey),
            buf -> new CharacterSelectC2SPacket(buf.readString())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public CharacterDefinition getCharacter() {
        return CharacterDefinition.fromKey(characterKey);
    }
}
