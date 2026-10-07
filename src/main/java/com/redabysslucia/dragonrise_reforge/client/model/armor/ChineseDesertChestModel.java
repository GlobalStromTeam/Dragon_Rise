package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.ChineseDesertChest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ChineseDesertChestModel extends GeoModel<ChineseDesertChest> {

    @Override
    public ResourceLocation getAnimationResource(ChineseDesertChest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(ChineseDesertChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/chinese_desert_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ChineseDesertChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/chinese_desert.png");
    }
}
