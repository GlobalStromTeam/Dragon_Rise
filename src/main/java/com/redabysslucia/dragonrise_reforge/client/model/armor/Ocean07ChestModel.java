package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Ocean07Chest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Ocean07ChestModel extends GeoModel<Ocean07Chest> {

    @Override
    public ResourceLocation getAnimationResource(Ocean07Chest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Ocean07Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ocean07_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Ocean07Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ocean07_chest.png");
    }
}