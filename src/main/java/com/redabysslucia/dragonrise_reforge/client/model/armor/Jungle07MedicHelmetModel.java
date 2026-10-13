package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07MedicHelmet;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Jungle07MedicHelmetModel extends GeoModel<Jungle07MedicHelmet> {

    @Override
    public ResourceLocation getAnimationResource(Jungle07MedicHelmet object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Jungle07MedicHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/jungle07_medic_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Jungle07MedicHelmet object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/jungle07_medic.png");
    }
}
