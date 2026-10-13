package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.RussianSoldierPants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RussianSoldierPantsModel extends GeoModel<RussianSoldierPants> {

    @Override
    public ResourceLocation getAnimationResource(RussianSoldierPants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(RussianSoldierPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/russian_soldier_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RussianSoldierPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/russian_soldier.png");
    }
}
