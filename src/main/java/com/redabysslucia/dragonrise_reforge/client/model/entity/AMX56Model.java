package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.AMX56Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AMX56Model extends VehicleModel<AMX56Entity> {

    private static final ResourceLocation[] MODEL_RESOURCES = {
            new ResourceLocation("dragonrise_reforge", "geo/amx56.geo.json"),
            new ResourceLocation("dragonrise_reforge", "geo/amx56nato.geo.json")
    };

    private static final ResourceLocation[] TEXTURE_RESOURCES = {
            new ResourceLocation("dragonrise_reforge", "textures/entity/amx56.png"),
            new ResourceLocation("dragonrise_reforge", "textures/entity/amx56nato.png")
    };

    private static final ResourceLocation ANIMATION_RESOURCE =
            new ResourceLocation("dragonrise_reforge", "animations/amx56.animation.json");

    @Override
    public ResourceLocation getModelResource(AMX56Entity entity) {
        if (entity != null) {
            int camoType = entity.getCamoType();
            if (camoType >= 0 && camoType < MODEL_RESOURCES.length) {
                return MODEL_RESOURCES[camoType];
            }
        }

        return MODEL_RESOURCES[0];
    }

    @Override
    public ResourceLocation getTextureResource(AMX56Entity entity) {
        if (entity != null) {
            int camoType = entity.getCamoType();
            if (camoType >= 0 && camoType < TEXTURE_RESOURCES.length) {
                return TEXTURE_RESOURCES[camoType];
            }
        }

        return TEXTURE_RESOURCES[0];
    }

    @Override
    public ResourceLocation getAnimationResource(AMX56Entity entity) {
        return ANIMATION_RESOURCE;
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}