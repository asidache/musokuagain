package com.reinkarnicaja.mod.equipment;

import com.reinkarnicaja.mod.ReincarnationMod;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Регистрация предметов экипировки
 */
public class EquipmentHandler {

    // Предметы
    public static Item KNIGHT_SWORD;
    public static Item BEGINNER_MAGIC_STAFF;
    public static Item RUDEUS_STAFF;
    public static Item ROXY_STAFF;
    public static Item SWORD_GOD_BLADE;
    public static Item DRAGON_ROOT_SWORD;
    public static Item MAGIC_ARMOR_MK1;
    public static Item BATTLE_GOD_ARMOR;
    public static Item BATTLE_GOD_GAUNTLETS;
    public static Item WATER_GOD_SWORD;
    public static Item DRAGON_CLAW;
    public static Item DRAGON_SCALE_ARMOR;
    public static Item KAJAKUTO_SWORD;

    /**
     * Регистрация всех предметов
     */
    public static void registerItems() {
        ToolMaterial diamondLike = new CustomToolMaterial(3, 1561, 8.0f, 3.0f, 22);

        KNIGHT_SWORD = register("knight_sword", new SwordItem(diamondLike, 3, -2.4f, new Item.Settings()));
        BEGINNER_MAGIC_STAFF = register("beginner_magic_staff", new Item(new Item.Settings()));
        RUDEUS_STAFF = register("rudeus_staff", new Item(new Item.Settings()));
        ROXY_STAFF = register("roxy_staff", new Item(new Item.Settings()));
        SWORD_GOD_BLADE = register("sword_god_blade", new SwordItem(diamondLike, 5, -2.0f, new Item.Settings()));
        DRAGON_ROOT_SWORD = register("dragon_root_sword", new SwordItem(diamondLike, 4, -2.2f, new Item.Settings()));
        MAGIC_ARMOR_MK1 = register("magic_armor_mk1", new Item(new Item.Settings()));
        BATTLE_GOD_ARMOR = register("battle_god_armor", new Item(new Item.Settings()));
        BATTLE_GOD_GAUNTLETS = register("battle_god_gauntlets", new Item(new Item.Settings()));
        WATER_GOD_SWORD = register("water_god_sword", new SwordItem(diamondLike, 4, -2.4f, new Item.Settings()));
        DRAGON_CLAW = register("dragon_claw", new Item(new Item.Settings()));
        DRAGON_SCALE_ARMOR = register("dragon_scale_armor", new Item(new Item.Settings()));
        KAJAKUTO_SWORD = register("kajakuto_sword", new SwordItem(diamondLike, 5, -2.0f, new Item.Settings()));

        // Регистрация способностей предметов будет в registerEquipmentAbilities()
    }

    private static Item register(String name, Item item) {
        Identifier id = Identifier.of(ReincarnationMod.MOD_ID, name);
        return Registry.register(Registries.ITEM, id, item);
    }

    /**
     * Регистрация способностей предметов (через события Fabric)
     */
    public static void registerEquipmentAbilities() {
        // Будет реализовано через UseItemCallback, AttackBlockCallback и т.д.
    }

    /**
     * Получить предмет по идентификатору
     */
    public static Item getByIdentifier(Identifier id) {
        return Registries.ITEM.get(id);
    }

    // Вспомогательный класс для материала инструмента
    private record CustomToolMaterial(int miningLevel, int durability, float miningSpeed, 
                                       float attackDamage, int enchantability) implements ToolMaterial {
        @Override
        public int getDurability() {
            return durability;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return miningSpeed;
        }

        @Override
        public float getAttackDamage() {
            return attackDamage;
        }

        @Override
        public int getMiningLevel() {
            return miningLevel;
        }

        @Override
        public int getEnchantability() {
            return enchantability;
        }

        @Override
        public net.minecraft.recipe.Ingredient getRepairIngredient() {
            return net.minecraft.recipe.Ingredient.EMPTY;
        }
    }
}
