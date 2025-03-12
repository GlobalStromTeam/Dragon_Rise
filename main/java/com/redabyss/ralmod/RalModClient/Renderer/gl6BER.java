package com.redabyss.ralmod.RalModClient.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.redabyss.ralmod.RalMod;
import com.redabyss.ralmod.RalModServer.BlockEntities.gl6BE;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.redabyss.ralmod.RalModClient.Model.gl6Model;

@OnlyIn(Dist.CLIENT)
public class gl6BER<T extends gl6BE> implements BlockEntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RalMod.MODID, "textures/block/gl6.png");
    private final ModelPart bottom;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart bracket;
    private final ModelPart connect;

    public gl6BER(BlockEntityRendererProvider.Context p_173666_) {
        ModelPart root = p_173666_.bakeLayer(gl6Model.LAYER);
        this.bottom = root.getChild("bottom");
        this.left = root.getChild("left");
        this.right = root.getChild("right");
        this.bracket = root.getChild("bracket");
        this.connect = this.bracket.getChild("connect");
    }

    @Override
    public void render(T blockentity, float partialTick, PoseStack posestack,
    MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        float YawAngle = blockentity.getYawAngle();
        float PitchAngle = blockentity.getPitchAngle();

        posestack.pushPose();
            posestack.translate(0.5, 0, 0.5);
            bottom.render(posestack, vertexconsumer, combinedLight, combinedOverlay);
        posestack.popPose();

        posestack.pushPose();
            posestack.translate(0.5, 0, 0.5);
            posestack.mulPose(Axis.YP.rotationDegrees(YawAngle));
            bracket.render(posestack, vertexconsumer, combinedLight, combinedOverlay);
        posestack.popPose();


        posestack.pushPose();
            posestack.translate(0.5, 0.6875, 0.5);
            posestack.mulPose(Axis.YP.rotationDegrees(YawAngle));
            posestack.mulPose(Axis.XP.rotationDegrees(PitchAngle));
            posestack.translate(-0.5,-0.6875, -0.5);
            posestack.translate(0.5,0,0.5);
            left.render(posestack, vertexconsumer, combinedLight, combinedOverlay);
            right.render(posestack, vertexconsumer, combinedLight, combinedOverlay);
        posestack.popPose();
    }

}
