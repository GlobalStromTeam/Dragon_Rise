package com.redabysslucia.dragonrise_reforge.init;

import com.mojang.blaze3d.platform.InputConstants;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum ModKeyMappings {

    // NIGHT_VISION("nightvisionkey",GLFW.GLFW_KEY_X,"Toggle Realistic Night Vision Mode"),
    ENGINE_CHANGE_MODE_TOGGLE("enginechangekey",GLFW.GLFW_KEY_V,"Toggle Engine Change Mode")
    ;

    private final String description;
    private final String translation;
    private final int key;
    private final boolean modifiable;
    private KeyMapping keybind;

    ModKeyMappings(int defaultKey) {
        this("", defaultKey, "");
    }

    ModKeyMappings(String description, int defaultKey, String translation) {
        this.description = Dragonrise_reforge.MODID + ".keyinfo." + description;
        this.key = defaultKey;
        this.modifiable = !description.isEmpty();
        this.translation = translation;
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (ModKeyMappings key : values())
            if (key.modifiable)
                consumer.accept(key.description, key.translation);
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        for (ModKeyMappings key : values()) {
            key.keybind = new KeyMapping(key.description, key.key, Dragonrise_reforge.MODID);
            if (!key.modifiable)
                continue;

            event.register(key.keybind);
        }
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance()
                .getWindow()
                .getWindow(), key);
    }

    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance()
                .getWindow()
                .getWindow(), button) == 1;
    }

    public static boolean ctrlDown() {
        return Screen.hasControlDown();
    }

    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean altDown() {
        return Screen.hasAltDown();
    }

    public KeyMapping getKeybind() {
        return keybind;
    }

    public boolean isClicked() {
        return keybind.consumeClick();
    }

    public boolean isPressed() {
        if (!modifiable)
            return isKeyDown(key);
        return keybind.isDown();
    }

    public String getBoundKey() {
        return keybind.getTranslatedKeyMessage()
                .getString()
                .toUpperCase();
    }

    public int getBoundCode() {
        return keybind.getKey()
                .getValue();
    }
}
