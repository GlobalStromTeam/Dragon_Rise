package com.redabysslucia.dragonrise_reforge.client.model.armor;

import com.redabysslucia.dragonrise_reforge.item.armor.Jungle07MedicChest;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Jungle07MedicChestModel extends GeoModel<Jungle07MedicChest> {

    @Override
    public ResourceLocation getAnimationResource(Jungle07MedicChest object) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(Jungle07MedicChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","geo/jungle07_medic_chest.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Jungle07MedicChest object) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/armor/jungle07_medic.png");
    }
}
