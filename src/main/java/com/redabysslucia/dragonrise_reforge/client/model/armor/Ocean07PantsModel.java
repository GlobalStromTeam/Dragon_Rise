package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Ocean07Pants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Ocean07PantsModel extends GeoModel<Ocean07Pants> {

    @Override
    public ResourceLocation getAnimationResource(Ocean07Pants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Ocean07Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/ocean07_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Ocean07Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/ocean07_pants.png");
    }
}