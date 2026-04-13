package com.redabysslucia.dragonrise_reforge.client.model.entity.projectile;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AirBomb500kgEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AirBomb500kgModel extends GeoModel<AirBomb500kgEntity> {

    @Override
    public ResourceLocation getAnimationResource(AirBomb500kgEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "animations/airbomb500kg.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(AirBomb500kgEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "geo/airbomb500kg.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AirBomb500kgEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/airbomb500kg.png");
    }
}