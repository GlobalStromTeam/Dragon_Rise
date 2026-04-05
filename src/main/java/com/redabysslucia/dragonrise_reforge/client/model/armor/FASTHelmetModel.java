package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.FASTHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FASTHelmetModel extends GeoModel<FASTHelmet> {

    @Override
    public ResourceLocation getAnimationResource(FASTHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(FASTHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/fast_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FASTHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/fast_helmet.png");
    }
}