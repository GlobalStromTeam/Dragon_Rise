package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.TerroristEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TerroristModel extends GeoModel<TerroristEntity> {

    @Override
    public ResourceLocation getAnimationResource(TerroristEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "animations/terrorist.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(TerroristEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "geo/terrorist.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TerroristEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/terrorist.png");
    }
}