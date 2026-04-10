package com.reinkarnicaja.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Сервер -> Клиент: Синхронизация данных игрока (мана, ранги, персонаж, бинды заклинаний).
 */
public record PlayerDataSyncS2CPacket(
        String characterKey,
        String activeStyleKey,
        int totalXP,
        float currentMana,
        float maxMana,
        float manaRegenRate,
        boolean isInDepletion,
        int calmanStationaryTicks,
        int debugRank,
        int dashCooldown,
        boolean hasPostDashBonus,
        boolean recentlyDashing,
        int dashReleaseTicks,
        String selectedSpellId,
        long spellSelectTime,
        boolean battleSpiritActive,
        boolean dualCastActive,
        java.util.Map<Integer, String> spellBinds
) implements CustomPayload {
    public static final Id<PlayerDataSyncS2CPacket> ID = new Id<>(Identifier.of("reinkarnicaja_mod", "player_data_sync_s2c"));
    
    public static final PacketCodec<RegistryByteBuf, PlayerDataSyncS2CPacket> CODEC = PacketCodec.of(
            (buf, packet) -> {
                buf.writeString(packet.characterKey);
                buf.writeString(packet.activeStyleKey);
                buf.writeInt(packet.totalXP);
                buf.writeFloat(packet.currentMana);
                buf.writeFloat(packet.maxMana);
                buf.writeFloat(packet.manaRegenRate);
                buf.writeBoolean(packet.isInDepletion);
                buf.writeInt(packet.calmanStationaryTicks);
                buf.writeInt(packet.debugRank);
                buf.writeInt(packet.dashCooldown);
                buf.writeBoolean(packet.hasPostDashBonus);
                buf.writeBoolean(packet.recentlyDashing);
                buf.writeInt(packet.dashReleaseTicks);
                buf.writeString(packet.selectedSpellId);
                buf.writeLong(packet.spellSelectTime);
                buf.writeBoolean(packet.battleSpiritActive);
                buf.writeBoolean(packet.dualCastActive);
                
                // Сериализация биндов заклинаний
                buf.writeVarInt(packet.spellBinds.size());
                for (java.util.Map.Entry<Integer, String> entry : packet.spellBinds.entrySet()) {
                    buf.writeVarInt(entry.getKey());
                    buf.writeString(entry.getValue());
                }
            },
            buf -> {
                int bindCount = buf.readVarInt();
                java.util.Map<Integer, String> spellBinds = new java.util.HashMap<>();
                for (int i = 0; i < bindCount; i++) {
                    int slotIndex = buf.readVarInt();
                    String spellId = buf.readString();
                    spellBinds.put(slotIndex, spellId);
                }
                
                return new PlayerDataSyncS2CPacket(
                    buf.readString(),
                    buf.readString(),
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readString(),
                    buf.readLong(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    spellBinds
                );
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
