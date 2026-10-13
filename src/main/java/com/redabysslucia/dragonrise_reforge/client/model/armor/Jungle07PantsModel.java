package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07Pants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Jungle07PantsModel extends GeoModel<Jungle07Pants> {

    @Override
    public ResourceLocation getAnimationResource(Jungle07Pants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Jungle07Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/jungle07_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Jungle07Pants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/jungle07.png");
    }
}
