package com.redabysslucia.dragonrise_reforge.client.model.item;

import com.redabysslucia.dragonrise_reforge.items.IedItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Ied 的手持模型 */
public class IedItemModel extends GeoModel<IedItem> {

    @Override
    public ResourceLocation getModelResource(IedItem animatable) {
        return new ResourceLocation("dragonrise_reforge", "geo/ied.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IedItem animatable) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/ied.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IedItem animatable) {
        return new ResourceLocation("dragonrise_reforge", "animations/bedrock/entity/ied.animation.json");
    }
}
