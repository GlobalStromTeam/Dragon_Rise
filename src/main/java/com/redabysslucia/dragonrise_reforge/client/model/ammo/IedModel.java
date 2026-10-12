package com.redabysslucia.dragonrise_reforge.client.model.ammo;

import com.redabysslucia.dragonrise_reforge.entities.IedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 土制遥控炸弹模型（模型与贴图来自桌面 ied.geo.json / ied.png） */
public class IedModel extends GeoModel<IedEntity> {

    @Override
    public ResourceLocation getModelResource(IedEntity animatable) {
        return new ResourceLocation("dragonrise_reforge", "geo/ied.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IedEntity animatable) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/ied.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IedEntity animatable) {
        return new ResourceLocation("dragonrise_reforge", "animations/bedrock/entity/ied.animation.json");
    }
}
