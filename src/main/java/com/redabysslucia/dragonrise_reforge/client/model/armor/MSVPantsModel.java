package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.MSVPants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MSVPantsModel extends GeoModel<MSVPants> {

    @Override
    public ResourceLocation getAnimationResource(MSVPants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(MSVPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/msv_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MSVPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/msv_pants.png");
    }
}