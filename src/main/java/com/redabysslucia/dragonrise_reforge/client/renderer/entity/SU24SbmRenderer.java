package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.renderer.sbm.DragonriseSbmVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.SU24Entity;
import com.redabysslucia.dragonrise_reforge.init.DragonriseBedrockLoader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SU24SbmRenderer extends DragonriseSbmVehicleRenderer<SU24Entity> {

    public SU24SbmRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getModelLocation(SU24Entity entity) {
        return DragonriseBedrockLoader.SU24_KEY;
    }

    @Override
    public ResourceLocation getTextureLocation(SU24Entity entity) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/su24.png");
    }
}
