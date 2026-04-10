package com.reinkarnicaja.mod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.reinkarnicaja.mod.ReincarnationMod;
import com.reinkarnicaja.mod.character.CharacterDefinition;
import com.reinkarnicaja.mod.data.CharacterAssignmentManager;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.rank.Rank;

/**
 * Команды мода: /setrank, /setcharacter, /givemana, /spawncurse
 */
public class ModCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                                CommandRegistryAccess registryAccess, 
                                CommandManager.RegistrationEnvironment environment) {
        
        // /setrank <0-6> - установить ранг игрока
        dispatcher.register(CommandManager.literal("setrank")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("rank", IntegerArgumentType.integer(0, 6))
                .executes(ModCommands::setRank)));

        // /setcharacter <player> <character> - назначить персонажа
        dispatcher.register(CommandManager.literal("setcharacter")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", StringArgumentType.word())
                .then(CommandManager.argument("character", StringArgumentType.word())
                    .executes(ModCommands::setCharacter))));

        // /givemana <amount> - дать ману
        dispatcher.register(CommandManager.literal("givemana")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 10000))
                .executes(ModCommands::giveMana)));

        // /spawncurse <boss_key> - заспавнить босса
        dispatcher.register(CommandManager.literal("spawncurse")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("bossKey", StringArgumentType.word())
                .executes(ModCommands::spawnCurse)));

        // /resetcharacter <player> - сбросить персонажа
        dispatcher.register(CommandManager.literal("resetcharacter")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", StringArgumentType.word())
                .executes(ModCommands::resetCharacter)));
    }

    private static int setRank(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int rankLevel = IntegerArgumentType.getInteger(context, "rank");
        ServerPlayerEntity player = context.getSource().getPlayer();
        
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Команда доступна только для игроков"), false);
            return 0;
        }

        PlayerData data = PlayerData.get(player);
        Rank rank = Rank.values()[rankLevel];
        
        // Установить всем стилям указанный ранг
        for (var style : data.getAllRanks().keySet()) {
            data.getStyleData().setRank(style, rank);
        }
        
        PlayerData.syncData(data, player);
        
        context.getSource().sendFeedback(() -> 
            Text.literal("Установлен ранг " + rank.getDisplayName() + " для " + player.getName().getString()), true);
        
        return 1;
    }

    private static int setCharacter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(context, "player");
        String characterKey = StringArgumentType.getString(context, "character");
        
        CharacterDefinition character = CharacterDefinition.fromKey(characterKey);
        if (character == null) {
            context.getSource().sendError(Text.literal("Неверный персонаж: " + characterKey));
            return 0;
        }

        if (CharacterAssignmentManager.isCharacterTaken(character)) {
            context.getSource().sendError(Text.literal("Персонаж " + character.name() + " уже занят"));
            return 0;
        }

        CharacterAssignmentManager.assign(playerName, character);
        
        context.getSource().sendFeedback(() -> 
            Text.literal(playerName + " назначен как " + character.getDisplayName()), true);
        
        ReincarnationMod.LOGGER.info("GM назначил {} как {}", playerName, character.name());
        
        return 1;
    }

    private static int giveMana(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        ServerPlayerEntity player = context.getSource().getPlayer();
        
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Команда доступна только для игроков"), false);
            return 0;
        }

        PlayerData data = PlayerData.get(player);
        data.setCurrentMana(Math.min(data.getMaxMana(), data.getCurrentMana() + amount));
        PlayerData.syncData(data, player);
        
        context.getSource().sendFeedback(() -> 
            Text.literal("Дано " + amount + " маны. Текущая мана: " + (int)data.getCurrentMana()), true);
        
        return 1;
    }

    private static int spawnCurse(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String bossKey = StringArgumentType.getString(context, "bossKey");
        ServerPlayerEntity player = context.getSource().getPlayer();
        
        if (player == null) {
            context.getSource().sendError(Text.literal("Команда доступна только для игроков"));
            return 0;
        }

        // TODO: реализовать спавн босса через BossSpawner
        context.getSource().sendFeedback(() -> 
            Text.literal("Спавн босса " + bossKey + " рядом с " + player.getName().getString()), true);
        
        return 1;
    }

    private static int resetCharacter(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(context, "player");
        
        if (CharacterAssignmentManager.hasAssignment(playerName)) {
            CharacterDefinition character = CharacterAssignmentManager.getAssignedCharacter(playerName);
            CharacterAssignmentManager.removeAssignment(playerName);
            
            context.getSource().sendFeedback(() -> 
                Text.literal("Сброшен персонаж " + character.name() + " у игрока " + playerName), true);
            
            ReincarnationMod.LOGGER.info("GM сбросил персонажа {} у {}", character.name(), playerName);
        } else {
            context.getSource().sendError(Text.literal("У игрока " + playerName + " нет назначенного персонажа"));
            return 0;
        }
        
        return 1;
    }
}
