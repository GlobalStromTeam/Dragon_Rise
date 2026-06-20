package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.renderer.sbm.DragonriseSbmVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.SU24Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import com.redabysslucia.dragonrise_reforge.init.DragonriseModelLoaders;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SU24SbmRenderer extends DragonriseSbmVehicleRenderer<SU24Entity> {

    public SU24SbmRenderer(EntityRendererProvider.Context context) {
        super(context, DragonriseModelLoaders.VEHICLE);
    }

    @Override
    public ResourceLocation getModelLocation(SU24Entity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "su24");
    }

    @Override
    public ResourceLocation getTextureLocation(SU24Entity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/su24.png");
    }
}
