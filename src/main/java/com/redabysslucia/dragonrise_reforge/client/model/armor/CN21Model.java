package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.CN21;
import com.redabysslucia.dragonrise_reforge.item.armor.CNfast;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CN21Model extends GeoModel<CN21> {

    @Override
    public ResourceLocation getAnimationResource(CN21 object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(CN21 object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/cn21helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CN21 object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/cn21helmet.png");
    }
}
