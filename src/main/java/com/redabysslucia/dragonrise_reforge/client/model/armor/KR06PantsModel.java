package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.KR06Pants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KR06PantsModel extends GeoModel<KR06Pants> {

    @Override
    public ResourceLocation getAnimationResource(KR06Pants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(KR06Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/kr06_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KR06Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/kr06_pants.png");
    }
}