package com.redabysslucia.dragonrise_reforge.integration;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

/**
 * Soft dependency on EsWeather: when thundersnow covers the vehicle, indirect FCS targeting
 * and auto-aim are jammed. No compile-time link to EsWeather.
 */
public final class EsWeatherFireControlBridge {
    private static final String MOD_ID = "esweather";
    private static final String API_CLASS = "org.esweather.api.EsWeatherAPI";
    private static final String API_METHOD = "isThundersnowAt";

    private static final boolean LOADED = ModList.get().isLoaded(MOD_ID);
    private static Method isThundersnowAtLevel;
    private static boolean lookupAttempted;

    private EsWeatherFireControlBridge() {
    }

    public static boolean isDisrupted(Entity entity) {
        if (!LOADED || entity == null) {
            return false;
        }
        Method method = resolveLevelMethod();
        if (method == null) {
            return false;
        }
        try {
            Object result = method.invoke(null, entity.level(), entity.getX(), entity.getZ());
            return result instanceof Boolean b && b;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    public static boolean isDisrupted(Level level, double x, double z) {
        if (!LOADED || level == null) {
            return false;
        }
        Method method = resolveLevelMethod();
        if (method == null) {
            return false;
        }
        try {
            Object result = method.invoke(null, level, x, z);
            return result instanceof Boolean b && b;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static Method resolveLevelMethod() {
        if (lookupAttempted) {
            return isThundersnowAtLevel;
        }
        lookupAttempted = true;
        try {
            Class<?> api = Class.forName(API_CLASS);
            isThundersnowAtLevel = api.getMethod(API_METHOD, Level.class, double.class, double.class);
        } catch (ReflectiveOperationException ignored) {
            isThundersnowAtLevel = null;
        }
        return isThundersnowAtLevel;
    }
}
