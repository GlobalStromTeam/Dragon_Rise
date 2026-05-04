package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.UNHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UNHelmetModel extends GeoModel<UNHelmet> {

    @Override
    public ResourceLocation getAnimationResource(UNHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(UNHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/un_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UNHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/un_helmet.png");
    }
}
