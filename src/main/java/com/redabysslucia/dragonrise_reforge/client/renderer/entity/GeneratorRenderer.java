package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.GeneratorEntity;
import com.redabysslucia.dragonrise_reforge.client.renderer.sbm.DragonriseSbmVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.init.DragonriseBedrockLoader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GeneratorRenderer extends DragonriseSbmVehicleRenderer<GeneratorEntity> {
    public GeneratorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getModelLocation(GeneratorEntity entity) {
        return DragonriseBedrockLoader.GENERATOR_KEY;
    }

    @Override
    public ResourceLocation getTextureLocation(GeneratorEntity entity) {
        return new ResourceLocation("dragonrise_reforge", "textures/entity/generator.png");
    }
}