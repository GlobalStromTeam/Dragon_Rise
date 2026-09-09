package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07Chest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Jungle07ChestModel extends GeoModel<Jungle07Chest> {

    @Override
    public ResourceLocation getAnimationResource(Jungle07Chest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Jungle07Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/jungle07_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Jungle07Chest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/jungle07.png");
    }
}
