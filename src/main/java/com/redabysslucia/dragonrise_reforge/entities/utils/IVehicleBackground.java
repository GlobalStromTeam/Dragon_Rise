package com.redabysslucia.dragonrise_reforge.entities.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IVehicleBackground {

    @OnlyIn(Dist.CLIENT)
    ResourceLocation getBackgroundTexture();

    @OnlyIn(Dist.CLIENT)
    default boolean shouldRenderBackground() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    default float getBackgroundAlpha() {
        return 1.0f;
    }
}
