package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.CNJustchest;
import com.redabysslucia.dragonrise_reforge.item.armor.CNchest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CNJustchestModel extends GeoModel<CNJustchest> {

    @Override
    public ResourceLocation getAnimationResource(CNJustchest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(CNJustchest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/cnjustchest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CNJustchest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/cnchest.png");
    }
}
