package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Ocean07Helmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Ocean07HelmetModel extends GeoModel<Ocean07Helmet> {

    @Override
    public ResourceLocation getAnimationResource(Ocean07Helmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Ocean07Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ocean07_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Ocean07Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ocean07_helmet.png");
    }
}