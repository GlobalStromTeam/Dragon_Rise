package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.renderer.sbm.DragonriseSbmVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.SU24MEntity;
import com.redabysslucia.dragonrise_reforge.init.DragonriseModelLoaders;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SU24MSbmRenderer extends DragonriseSbmVehicleRenderer<SU24MEntity> {

    public SU24MSbmRenderer(EntityRendererProvider.Context context) {
        super(context, DragonriseModelLoaders.VEHICLE);
    }

    @Override
    public ResourceLocation getModelLocation(SU24MEntity entity) {
        return new ResourceLocation("dragonrise_reforge", "su24");
    }

    @Override
    public ResourceLocation getTextureLocation(SU24MEntity entity) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/su24.png");
    }
}