package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.UkrainianSoldierPants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UkrainianSoldierPantsModel extends GeoModel<UkrainianSoldierPants> {

    @Override
    public ResourceLocation getAnimationResource(UkrainianSoldierPants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(UkrainianSoldierPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ukrainian_soldier_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UkrainianSoldierPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ukrainian_soldier.png");
    }
}
