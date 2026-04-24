package com.redabysslucia.dragonrise_reforge.client.model.ammo;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ammo.NukerBombEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NukerBombModel extends GeoModel<NukerBombEntity> {
    public NukerBombModel() {
    }

    public ResourceLocation getAnimationResource(NukerBombEntity entity) {
        return null;
    }

    public ResourceLocation getModelResource(NukerBombEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "geo/nuclearbomb.geo.json");
    }

    public ResourceLocation getTextureResource(NukerBombEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/nuclearbomb.png");
    }
}
