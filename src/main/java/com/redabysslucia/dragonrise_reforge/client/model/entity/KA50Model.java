package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.KA50Entity;
import net.minecraft.resources.ResourceLocation;

public class KA50Model extends VehicleModel<KA50Entity> {

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public ResourceLocation getModelResource(KA50Entity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID,"geo/ka50.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KA50Entity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID,"textures/entity/ka50.png");
    }
}