package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.UkrainianSoldierChest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UkrainianSoldierChestModel extends GeoModel<UkrainianSoldierChest> {

    @Override
    public ResourceLocation getAnimationResource(UkrainianSoldierChest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(UkrainianSoldierChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ukrainian_soldier_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UkrainianSoldierChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ukrainian_soldier.png");
    }
}
