package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.CNfast;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CNfastModel extends GeoModel<CNfast> {

    @Override
    public ResourceLocation getAnimationResource(CNfast object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(CNfast object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/cnfast.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CNfast object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/cnfast.png");
    }
}
