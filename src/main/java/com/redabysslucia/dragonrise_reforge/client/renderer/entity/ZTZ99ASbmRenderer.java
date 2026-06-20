package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.renderer.sbm.DragonriseSbmVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import com.redabysslucia.dragonrise_reforge.init.DragonriseModelLoaders;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ZTZ99ASbmRenderer extends DragonriseSbmVehicleRenderer<ZTZ99AEntity> {

    public ZTZ99ASbmRenderer(EntityRendererProvider.Context context) {
        super(context, DragonriseModelLoaders.VEHICLE);
    }

    @Override
    public ResourceLocation getModelLocation(ZTZ99AEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "ztz99a");
    }

    @Override
    public ResourceLocation getTextureLocation(ZTZ99AEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/ztz99a.png");
    }
}
