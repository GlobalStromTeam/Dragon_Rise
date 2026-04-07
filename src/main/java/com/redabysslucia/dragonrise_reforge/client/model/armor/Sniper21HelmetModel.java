package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.armor.Sniper21Helmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Sniper21HelmetModel extends GeoModel<Sniper21Helmet> {

    @Override
    public ResourceLocation getAnimationResource(Sniper21Helmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Sniper21Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/sniper21_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Sniper21Helmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/sniper21_helmet.png");
    }
}