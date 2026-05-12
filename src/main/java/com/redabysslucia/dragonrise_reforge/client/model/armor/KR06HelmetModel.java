package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.KR06Helmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KR06HelmetModel extends GeoModel<KR06Helmet> {

    @Override
    public ResourceLocation getAnimationResource(KR06Helmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(KR06Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/kr06_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KR06Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/kr06_helmet.png");
    }
}