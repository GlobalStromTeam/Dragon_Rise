package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Gorka3Leggings;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Gorka3LeggingsModel extends GeoModel<Gorka3Leggings> {

    @Override
    public ResourceLocation getAnimationResource(Gorka3Leggings object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Gorka3Leggings object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/gorka3_leggings.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Gorka3Leggings object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/gorka3_leggings.png");
    }
}