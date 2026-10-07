package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.ChineseDesertHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ChineseDesertHelmetModel extends GeoModel<ChineseDesertHelmet> {

    @Override
    public ResourceLocation getAnimationResource(ChineseDesertHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(ChineseDesertHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/chinese_desert_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ChineseDesertHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/chinese_desert.png");
    }
}
