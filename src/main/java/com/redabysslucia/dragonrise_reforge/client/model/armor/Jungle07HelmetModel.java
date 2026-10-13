package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07Helmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Jungle07HelmetModel extends GeoModel<Jungle07Helmet> {

    @Override
    public ResourceLocation getAnimationResource(Jungle07Helmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Jungle07Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/jungle07_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Jungle07Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/jungle07.png");
    }
}
