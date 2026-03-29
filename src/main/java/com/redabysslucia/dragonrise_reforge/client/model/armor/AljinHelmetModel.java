package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.AljinHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AljinHelmetModel extends GeoModel<AljinHelmet> {

    @Override
    public ResourceLocation getAnimationResource(AljinHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(AljinHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/aljin_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AljinHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/aljin_helmet.png");
    }
}