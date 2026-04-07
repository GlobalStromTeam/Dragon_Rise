package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Pants21;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pants21Model extends GeoModel<Pants21> {

    @Override
    public ResourceLocation getAnimationResource(Pants21 object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Pants21 object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/pants21.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pants21 object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/pants21.png");
    }
}