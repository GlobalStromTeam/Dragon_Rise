package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.RussianSoldierChest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RussianSoldierChestModel extends GeoModel<RussianSoldierChest> {

    @Override
    public ResourceLocation getAnimationResource(RussianSoldierChest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(RussianSoldierChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/russian_soldier_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RussianSoldierChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/russian_soldier.png");
    }
}
