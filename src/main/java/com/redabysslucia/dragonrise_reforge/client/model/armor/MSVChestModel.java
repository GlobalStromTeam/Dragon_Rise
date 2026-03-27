package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.MSVChest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MSVChestModel extends GeoModel<MSVChest> {

    @Override
    public ResourceLocation getAnimationResource(MSVChest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(MSVChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/msv_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MSVChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/msv_chest.png");
    }
}