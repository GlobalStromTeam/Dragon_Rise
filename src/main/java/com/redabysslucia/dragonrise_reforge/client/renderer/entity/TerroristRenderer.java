package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.model.entity.TerroristModel;
import com.redabysslucia.dragonrise_reforge.entities.TerroristEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TerroristRenderer extends GeoEntityRenderer<TerroristEntity> {
    public TerroristRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TerroristModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(TerroristEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    protected float getDeathMaxRotation(TerroristEntity entityLivingBaseIn) {
        return 0;
    }
}
