package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Desert07Helmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Desert07HelmetModel extends GeoModel<Desert07Helmet> {

    @Override
    public ResourceLocation getAnimationResource(Desert07Helmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Desert07Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/desert07_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Desert07Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/desert07_helmet.png");
    }
}