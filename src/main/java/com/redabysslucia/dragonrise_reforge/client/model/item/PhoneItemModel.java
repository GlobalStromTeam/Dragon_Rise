package com.redabysslucia.dragonrise_reforge.client.model.item;

import com.redabysslucia.dragonrise_reforge.items.PhoneItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Phone 的手持模型 */
public class PhoneItemModel extends GeoModel<PhoneItem> {

    @Override
    public ResourceLocation getModelResource(PhoneItem animatable) {
        return new ResourceLocation("dragonrise_reforge", "geo/phone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PhoneItem animatable) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/phone.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PhoneItem animatable) {
        return new ResourceLocation("dragonrise_reforge", "animations/bedrock/entity/ied.animation.json");
    }
}
