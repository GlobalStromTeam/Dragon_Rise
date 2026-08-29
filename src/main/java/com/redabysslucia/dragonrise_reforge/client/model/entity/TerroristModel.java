package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.TerroristEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TerroristModel extends GeoModel<TerroristEntity> {

    @Override
    public ResourceLocation getAnimationResource(TerroristEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "animations/terrorist.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(TerroristEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "geo/terrorist.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TerroristEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/terrorist.png");
    }
}
