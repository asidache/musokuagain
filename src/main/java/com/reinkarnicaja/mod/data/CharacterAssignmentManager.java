package com.reinkarnicaja.mod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.reinkarnicaja.mod.character.CharacterDefinition;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JSON-хранилище назначений персонажей за игроками.
 * Файл: character_assignments.json
 */
public class CharacterAssignmentManager {
    private static final String FILENAME = "character_assignments.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static final Map<String, String> playerNameToCharacter = new HashMap<>();
    private static final Set<String> takenCharacters = new HashSet<>();
    
    private static File dataDir;

    /**
     * Загрузка данных из файла
     */
    public static void load(File serverDir) {
        dataDir = new File(serverDir, "reinkarnicaja_mod");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        File file = new File(dataDir, FILENAME);
        if (!file.exists()) {
            // Создаём пустой файл
            save();
            return;
        }
        
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = GSON.fromJson(reader, type);
            
            if (loaded != null) {
                playerNameToCharacter.clear();
                takenCharacters.clear();
                
                for (Map.Entry<String, String> entry : loaded.entrySet()) {
                    playerNameToCharacter.put(entry.getKey(), entry.getValue());
                    takenCharacters.add(entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Сохранение данных в файл
     */
    public static void save() {
        if (dataDir == null) return;
        
        File file = new File(dataDir, FILENAME);
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(playerNameToCharacter, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Назначить персонажа игроку
     */
    public static boolean assign(String playerName, CharacterDefinition character) {
        if (isCharacterTaken(character)) {
            return false;
        }
        
        if (hasAssignment(playerName)) {
            // Снять предыдущее назначение
            removeAssignment(playerName);
        }
        
        playerNameToCharacter.put(playerName, character.getKey());
        takenCharacters.add(character.getKey());
        save();
        return true;
    }

    /**
     * Проверить есть ли у игрока назначение
     */
    public static boolean hasAssignment(String playerName) {
        return playerNameToCharacter.containsKey(playerName);
    }

    /**
     * Получить назначенного персонажа игрока
     */
    public static CharacterDefinition getAssignedCharacter(String playerName) {
        String charKey = playerNameToCharacter.get(playerName);
        if (charKey == null) {
            return null;
        }
        return CharacterDefinition.fromKey(charKey);
    }

    /**
     * Проверить занят ли персонаж
     */
    public static boolean isCharacterTaken(CharacterDefinition character) {
        return takenCharacters.contains(character.getKey());
    }

    /**
     * Получить всех занятых персонажей
     */
    public static Set<CharacterDefinition> getTakenCharacters() {
        Set<CharacterDefinition> result = new HashSet<>();
        for (String key : takenCharacters) {
            result.add(CharacterDefinition.fromKey(key));
        }
        return result;
    }

    /**
     * Снять назначение с игрока
     */
    public static void removeAssignment(String playerName) {
        String charKey = playerNameToCharacter.remove(playerName);
        if (charKey != null) {
            takenCharacters.remove(charKey);
            save();
        }
    }

    /**
     * Очистить все данные (для тестов)
     */
    public static void clear() {
        playerNameToCharacter.clear();
        takenCharacters.clear();
    }
}
