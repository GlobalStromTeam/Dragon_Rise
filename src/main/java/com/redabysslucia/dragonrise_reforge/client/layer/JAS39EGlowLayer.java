package com.redabysslucia.dragonrise_reforge.client.layer;


import com.atsuishio.superbwarfare.client.renderer.ModRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import com.redabysslucia.dragonrise_reforge.entities.JAS39EEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class JAS39EGlowLayer extends GeoRenderLayer<JAS39EEntity> {

        private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/entity/glow/jas39e_glow.png");

    public JAS39EGlowLayer(GeoRenderer<JAS39EEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, JAS39EEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType glowRenderType = ModRenderTypes.LASER.apply(LAYER);
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, glowRenderType, bufferSource.getBuffer(glowRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
