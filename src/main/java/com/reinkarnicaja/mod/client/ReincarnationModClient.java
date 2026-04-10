package com.reinkarnicaja.mod.client;

import com.reinkarnicaja.mod.ReincarnationMod;
import com.reinkarnicaja.mod.data.PlayerData;
import com.reinkarnicaja.mod.network.BattleSpiritC2SPacket;
import com.reinkarnicaja.mod.network.CharacterSelectS2CPacket;
import com.reinkarnicaja.mod.network.SpellBindC2SPacket;
import com.reinkarnicaja.mod.spell.SpellData;
import com.reinkarnicaja.mod.spell.SpellManager;
import com.reinkarnicaja.mod.ui.CharacterSelectScreen;
import com.reinkarnicaja.mod.ui.SpellRadialMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReincarnationModClient implements ClientModInitializer {
    public static final String MOD_ID = "reinkarnicaja_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Key bindings
    public static final KeyBinding OPEN_CHARACTER_SELECT_KEY = new FabricKeyBinding(
        "key.reinkarnicaja_mod.open_character_select",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        "category.reinkarnicaja_mod.general"
    );

    public static final KeyBinding OPEN_SPELL_MENU_KEY = new FabricKeyBinding(
        "key.reinkarnicaja_mod.open_spell_menu",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "category.reinkarnicaja_mod.general"
    );

    public static final KeyBinding TOGGLE_BATTLE_SPIRIT_KEY = new FabricKeyBinding(
        "key.reinkarnicaja_mod.toggle_battle_spirit",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_B,
        "category.reinkarnicaja_mod.general"
    );

    // Bind заклинаний на клавиши 1-9 (теперь регистрируются через ModKeyBindings)
    public static final KeyBinding[] SPELL_BIND_KEYS = com.reinkarnicaja.mod.ModKeyBindings.SPELL_SLOTS;

    // Локальное хранилище биндов заклинаний (клиент)
    private static final Map<Integer, String> clientSpellBinds = new HashMap<>();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Reincarnation Mod Client v1.0.0");

        // Регистрация key bindings через ModKeyBindings
        com.reinkarnicaja.mod.ModKeyBindings.register();
        
        // Регистрация старых key bindings
        KeyBindingRegistry.INSTANCE.register(OPEN_CHARACTER_SELECT_KEY);
        KeyBindingRegistry.INSTANCE.register(OPEN_SPELL_MENU_KEY);
        KeyBindingRegistry.INSTANCE.register(TOGGLE_BATTLE_SPIRIT_KEY);

        // Обработчик тиков клиента
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (OPEN_CHARACTER_SELECT_KEY.wasPressed()) {
                // Открытие экрана выбора персонажа
                if (client.currentScreen == null) {
                    client.setScreen(new CharacterSelectScreen());
                }
            }

            while (OPEN_SPELL_MENU_KEY.wasPressed()) {
                // Открытие радиального меню заклинаний
                SpellRadialMenu.open(client.player);
            }

            while (TOGGLE_BATTLE_SPIRIT_KEY.wasPressed()) {
                // Переключение боевого духа
                PlayerData data = PlayerData.get(client.player);
                boolean currentlyActive = data.isBattleSpiritActive();
                ClientPlayNetworking.send(new BattleSpiritC2SPacket(!currentlyActive));
            }

            // Обработка биндов заклинаний (клавиши 1-9 через ModKeyBindings)
            for (int i = 0; i < SPELL_BIND_KEYS.length; i++) {
                while (SPELL_BIND_KEYS[i].wasPressed()) {
                    bindSpellToSlot(client.player, i);
                }
            }
        });

        // Обработка пакета выбора персонажа от сервера
        ClientPlayNetworking.registerGlobalReceiver(CharacterSelectS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen == null) {
                    context.client().setScreen(new CharacterSelectScreen());
                }
            });
        });

        LOGGER.info("Reincarnation Mod Client initialized successfully");
    }

    /**
     * Привязать заклинание к слоту (клавиша 1-9)
     */
    private void bindSpellToSlot(net.minecraft.client.network.ClientPlayerEntity player, int slotIndex) {
        PlayerData data = PlayerData.get(player);
        List<SpellData> availableSpells = SpellManager.getAvailableSpells(data);
        
        if (availableSpells.isEmpty()) {
            LOGGER.warn("Нет доступных заклинаний для бинда");
            return;
        }

        // Циклический перебор заклинаний при повторном нажатии
        String currentSpellId = clientSpellBinds.get(slotIndex);
        int currentIndex = -1;
        
        if (currentSpellId != null) {
            for (int i = 0; i < availableSpells.size(); i++) {
                if (availableSpells.get(i).id().equals(currentSpellId)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        // Выбрать следующее заклинание
        int nextIndex = (currentIndex + 1) % availableSpells.size();
        SpellData nextSpell = availableSpells.get(nextIndex);
        
        // Сохранить локально
        clientSpellBinds.put(slotIndex, nextSpell.id());
        
        // Отправить на сервер
        ClientPlayNetworking.send(new SpellBindC2SPacket(slotIndex, nextSpell.id()));
        
        LOGGER.info("Заклинание {} привязано к слоту {}", nextSpell.name(), slotIndex + 1);
    }

    /**
     * Получить заклинание из слота (для использования)
     */
    public static String getSpellFromBind(int slotIndex) {
        return clientSpellBinds.get(slotIndex);
    }

    /**
     * Установить бинд из пакета синхронизации
     */
    public static void setSpellBind(int slotIndex, String spellId) {
        if (spellId == null || spellId.isEmpty()) {
            clientSpellBinds.remove(slotIndex);
        } else {
            clientSpellBinds.put(slotIndex, spellId);
        }
    }

    /**
     * Очистить все бинды
     */
    public static void clearAllBinds() {
        clientSpellBinds.clear();
    }
}
