package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.client.renderer.armor.CNchestRenderer;
import com.redabysslucia.dragonrise_reforge.item.armor.CNchest;
import com.redabysslucia.dragonrise_reforge.client.model.armor.CNchestModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CNchestModel extends GeoModel<CNchest> {

    @Override
    public ResourceLocation getAnimationResource(CNchest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(CNchest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/cnchest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CNchest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/cnchest.png");
    }
}
