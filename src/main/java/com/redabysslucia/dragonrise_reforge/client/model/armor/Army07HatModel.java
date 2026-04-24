package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Army07Hat;
import com.redabysslucia.dragonrise_reforge.item.armor.Ocean07Helmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Army07HatModel extends GeoModel<Army07Hat> {

    @Override
    public ResourceLocation getAnimationResource(Army07Hat object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Army07Hat object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ocean07_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Army07Hat object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ocean07_helmet.png");
    }
}