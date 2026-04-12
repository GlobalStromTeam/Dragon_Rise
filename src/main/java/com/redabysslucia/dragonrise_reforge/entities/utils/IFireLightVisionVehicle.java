package com.redabysslucia.dragonrise_reforge.entities.utils;

import net.minecraft.resources.ResourceLocation;

public interface IFireLightVisionVehicle {

    ResourceLocation getNightVisionShader();

    ResourceLocation getThermalVisionShader();

    boolean getNVEnable();

    void setNVEnable(boolean enable);

    boolean getTVGEnable();

    void setTVGEnable(boolean enable);

}
