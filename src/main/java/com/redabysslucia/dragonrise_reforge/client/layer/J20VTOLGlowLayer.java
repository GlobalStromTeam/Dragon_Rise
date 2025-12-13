package com.redabysslucia.dragonrise_reforge.client.layer;


import com.atsuishio.superbwarfare.client.renderer.ModRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redabysslucia.dragonrise_reforge.entities.J20VTOLEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class J20VTOLGlowLayer extends GeoRenderLayer<J20VTOLEntity> {

    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath("dragonrise_reforge","textures/entity/glow/j20_glow.png");

    public J20VTOLGlowLayer(GeoRenderer<J20VTOLEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, J20VTOLEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType glowRenderType = ModRenderTypes.LASER.apply(LAYER);
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, glowRenderType, bufferSource.getBuffer(glowRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
