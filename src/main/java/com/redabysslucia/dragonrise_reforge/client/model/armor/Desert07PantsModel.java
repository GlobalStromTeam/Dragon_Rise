package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Desert07Pants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Desert07PantsModel extends GeoModel<Desert07Pants> {

    @Override
    public ResourceLocation getAnimationResource(Desert07Pants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Desert07Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/desert07_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Desert07Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/desert07_pants.png");
    }
}