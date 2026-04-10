package com.reinkarnicaja.mod;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final String CATEGORY = "key.categories.reinkarnicaja";
    
    // 9 слотов для заклинаний
    public static final KeyBinding[] SPELL_SLOTS = new KeyBinding[9];

    public static void register() {
        for (int i = 0; i < 9; i++) {
            final int index = i;
            SPELL_SLOTS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reinkarnicaja.spell_slot_" + (i + 1),
                InputUtil.Type.KEYSYM,
                getDefaultKeyCode(i),
                CATEGORY
            ));
        }
    }

    private static int getDefaultKeyCode(int index) {
        // Стандартные клавиши 1-9 по умолчанию
        return GLFW.GLFW_KEY_1 + index;
    }

    public static KeyBinding getSlotKeyBinding(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 9) return null;
        return SPELL_SLOTS[slotIndex];
    }
}
