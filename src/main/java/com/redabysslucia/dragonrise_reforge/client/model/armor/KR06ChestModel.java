package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.KR06Chest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KR06ChestModel extends GeoModel<KR06Chest> {

    @Override
    public ResourceLocation getAnimationResource(KR06Chest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(KR06Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/kr06_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KR06Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/kr06_chest.png");
    }
}