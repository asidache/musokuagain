package com.reinkarnicaja.mod.boss;

import com.reinkarnicaja.mod.ReincarnationMod;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

/**
 * Менеджер дропа предметов с боссов
 */
public class BossDropManager {

    /**
     * Выдать награду за убийство босса
     * @param world мир
     * @param bossData данные босса
     * @param dropPosition позиция дропа
     */
    public static void giveBossRewards(ServerWorld world, BossData bossData, Vec3d dropPosition) {
        BossDefinition bossDef = bossData.getDefinition();
        
        // Создать стопки наград
        java.util.List<ItemStack> rewards = getRewardItems(bossDef);
        
        // Заспавнить предметы
        for (ItemStack stack : rewards) {
            ItemEntity itemEntity = new ItemEntity(
                world,
                dropPosition.x,
                dropPosition.y,
                dropPosition.z,
                stack
            );
            
            // Разброс предметов
            itemEntity.setVelocity(
                world.random.nextGaussian() * 0.1,
                0.2,
                world.random.nextGaussian() * 0.1
            );
            
            world.spawnEntity(itemEntity);
        }
        
        ReincarnationMod.LOGGER.info("Boss {} dropped {} items", bossDef.getKey(), rewards.size());
    }

    /**
     * Получить список предметов награды для босса
     */
    private static java.util.List<ItemStack> getRewardItems(BossDefinition bossDef) {
        java.util.List<ItemStack> rewards = new java.util.ArrayList<>();
        
        switch (bossDef) {
            case GOLDEN_MAGE:
                // Золотой маг: опыт, изумруды
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 5));
                rewards.add(new ItemStack(Items.EMERALD, 3));
                break;
                
            case MAGIC_KNIGHT:
                // Магический рыцарь: опыт, железный слиток, книга
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 8));
                rewards.add(new ItemStack(Items.IRON_INGOT, 5));
                rewards.add(new ItemStack(Items.BOOK, 2));
                break;
                
            case EARTH_SERPENT:
                // Земляной змей: опыт, алмазы
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 10));
                rewards.add(new ItemStack(Items.DIAMOND, 2));
                break;
                
            case HYDRA:
                // Гидра: опыт, незеритовый слиток, зелье
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 15));
                rewards.add(new ItemStack(Items.NETHERITE_INGOT, 1));
                rewards.add(new ItemStack(Items.POTION, 3));
                break;
                
            case HOLY_SWORD_DEMON:
                // Демон святого меча: опыт, незеритовый меч, тотем
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 20));
                rewards.add(new ItemStack(Items.NETHERITE_SWORD, 1));
                rewards.add(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                break;
                
            case ANCIENT_DRAGON:
                // Древний дракон: опыт, яйцо дракона, звезда Незера
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 30));
                rewards.add(new ItemStack(Items.DRAGON_EGG, 1));
                rewards.add(new ItemStack(Items.NETHER_STAR, 1));
                break;
                
            case DEMON_LORD_LAPLACE:
                // Владыка демонов Лаплас: опыт, незеритовая броня, маяк
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 50));
                rewards.add(new ItemStack(Items.NETHERITE_CHESTPLATE, 1));
                rewards.add(new ItemStack(Items.BEACON, 1));
                break;
        }
        
        return rewards;
    }

    /**
     * Объявить о получении награды
     */
    public static void announceReward(ServerWorld world, String playerName, BossDefinition bossDef) {
        Text message = Text.translatable("boss.reinkarnicaja_mod.reward_received",
                                         Text.literal(playerName),
                                         Text.translatable("boss.reinkarnicaja_mod." + bossDef.getKey()))
            .formatted(Formatting.GOLD);
        
        for (var player : world.getPlayers()) {
            player.sendMessage(message, true);
        }
    }
}
