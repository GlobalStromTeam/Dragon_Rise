package com.redabysslucia.dragonrise_reforge.client.layer;


import com.atsuishio.superbwarfare.client.renderer.ModRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redabysslucia.dragonrise_reforge.entities.J15TEntity;
import com.redabysslucia.dragonrise_reforge.entities.J16Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class J15TGlowLayer extends GeoRenderLayer<J15TEntity> {

    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/entity/glow/j15t_glow.png");

    public J15TGlowLayer(GeoRenderer<J15TEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, J15TEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType glowRenderType = ModRenderTypes.LASER.apply(LAYER);
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, glowRenderType, bufferSource.getBuffer(glowRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
