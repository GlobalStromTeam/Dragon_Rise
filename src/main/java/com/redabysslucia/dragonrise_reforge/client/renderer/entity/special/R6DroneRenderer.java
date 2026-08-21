package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import com.redabysslucia.dragonrise_reforge.resource.model.EntityModelReloadListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 侦察无人车渲染器：SBM 加载 models/bedrock/entity/r6_drone.geo.json。
 * 车体朝向：控制端直接用玩家视角（即时跟随、与相机一致），其他客户端用同步角度。
 */
public class R6DroneRenderer extends EntityRenderer<R6DroneEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation("dragonrise_reforge", "models/bedrock/entity/r6_drone.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("dragonrise_reforge", "textures/entity/r6_drone.png");

    public R6DroneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(R6DroneEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(R6DroneEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        var model = EntityModelReloadListener.INSTANCE.getModel(MODEL);
        if (model == null) return;

        poseStack.pushPose();

        // 车体朝向：控制端玩家视角（即时），否则同步角度
        float bodyYaw = entity.getRenderYaw(partialTick);
        float bodyXRot = entity.getRenderPitch(partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-bodyXRot));

        // 模型略微下沉使其贴地（实体原点在脚底）
        poseStack.translate(0.0, -0.06, 0.0);

        model.renderToBuffer(
                poseStack,
                buffer,
                RenderType.entityCutout(TEXTURE),
                BedrockModelRenderTypes.polyMeshCutout(TEXTURE),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
