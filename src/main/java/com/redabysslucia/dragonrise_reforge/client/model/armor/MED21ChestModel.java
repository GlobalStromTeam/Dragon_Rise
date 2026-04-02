package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.MED21Chest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MED21ChestModel extends GeoModel<MED21Chest> {

    @Override
    public ResourceLocation getAnimationResource(MED21Chest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(MED21Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/med21_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MED21Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/med21_chest.png");
    }
}