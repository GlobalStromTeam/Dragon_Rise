package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.UkrainianSoldierHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UkrainianSoldierHelmetModel extends GeoModel<UkrainianSoldierHelmet> {

    @Override
    public ResourceLocation getAnimationResource(UkrainianSoldierHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(UkrainianSoldierHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ukrainian_soldier_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UkrainianSoldierHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ukrainian_soldier.png");
    }
}
