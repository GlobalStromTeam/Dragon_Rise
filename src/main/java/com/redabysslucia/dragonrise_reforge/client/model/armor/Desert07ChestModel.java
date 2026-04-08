package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Desert07Chest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Desert07ChestModel extends GeoModel<Desert07Chest> {

    @Override
    public ResourceLocation getAnimationResource(Desert07Chest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Desert07Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/desert07_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Desert07Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/desert07_chest.png");
    }
}