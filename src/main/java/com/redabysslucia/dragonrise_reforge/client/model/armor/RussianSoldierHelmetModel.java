package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.RussianSoldierHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RussianSoldierHelmetModel extends GeoModel<RussianSoldierHelmet> {

    @Override
    public ResourceLocation getAnimationResource(RussianSoldierHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(RussianSoldierHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/russian_soldier_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RussianSoldierHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/russian_soldier.png");
    }
}
