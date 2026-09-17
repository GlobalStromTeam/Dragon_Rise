package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07MedicPants;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Jungle07MedicPantsModel extends GeoModel<Jungle07MedicPants> {

    @Override
    public ResourceLocation getAnimationResource(Jungle07MedicPants object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Jungle07MedicPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/jungle07_medic_pants.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Jungle07MedicPants object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/jungle07_medic.png");
    }
}
