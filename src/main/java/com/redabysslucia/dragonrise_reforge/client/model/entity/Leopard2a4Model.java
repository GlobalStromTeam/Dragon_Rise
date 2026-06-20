package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.AMX56Entity;
import com.redabysslucia.dragonrise_reforge.entities.Leopard2a4Entity;
import net.minecraft.resources.ResourceLocation;

public class Leopard2a4Model extends DragonriseVehicleModel<Leopard2a4Entity> {

    private static final ResourceLocation[] TEXTURE_RESOURCES = {
            ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/leopard2a4.png"),
            ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/leopard2a4nato.png"),
    };

    @Override
    public ResourceLocation getTextureResource(Leopard2a4Entity entity) {
        if (entity != null) {
            int camoType = entity.getCamoType();
            if (camoType >= 0 && camoType < TEXTURE_RESOURCES.length) {
                return TEXTURE_RESOURCES[camoType];
            }
        }

        return TEXTURE_RESOURCES[0];
    }

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return false;
    }
}
