package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.renderer.sbm.DragonriseSbmVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.SU24MEntity;
import com.redabysslucia.dragonrise_reforge.init.DragonriseBedrockLoader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SU24MSbmRenderer extends DragonriseSbmVehicleRenderer<SU24MEntity> {

    public SU24MSbmRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getModelLocation(SU24MEntity entity) {
        return DragonriseBedrockLoader.SU24M_KEY;
    }

    @Override
    public ResourceLocation getTextureLocation(SU24MEntity entity) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/su24m.png");
    }
}