package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Gorka3;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Gorka3Model extends GeoModel<Gorka3> {

    @Override
    public ResourceLocation getAnimationResource(Gorka3 object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Gorka3 object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/gorka3.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Gorka3 object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/gorka3.png");
    }
}