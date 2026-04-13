package com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile;

import com.redabysslucia.dragonrise_reforge.client.model.entity.projectile.AirBomb500kgModel;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AirBomb500kgEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AirBomb500kgRenderer extends GeoEntityRenderer<AirBomb500kgEntity> {
    public AirBomb500kgRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AirBomb500kgModel());
    }

    @Override
    public RenderType getRenderType(AirBomb500kgEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public void render(AirBomb500kgEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())));
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }
}