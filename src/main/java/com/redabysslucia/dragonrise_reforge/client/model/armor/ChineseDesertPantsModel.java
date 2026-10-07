package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.ChineseDesertPants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ChineseDesertPantsModel extends GeoModel<ChineseDesertPants> {

    @Override
    public ResourceLocation getAnimationResource(ChineseDesertPants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(ChineseDesertPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/chinese_desert_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ChineseDesertPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/chinese_desert.png");
    }
}
