package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.entities.special.ClusterChargeEntity;
import com.redabysslucia.dragonrise_reforge.resource.model.ProjectileModelReloadListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * 霰射炸药渲染器：使用 SBM（SimpleBedrockModel）加载 models/bedrock/projectile/cluster_charge.geo.json，
 * 旋转方式参照 superbwarfare EDDRenderer，使其贴合墙面摆放。
 */
public class ClusterChargeRenderer extends EntityRenderer<ClusterChargeEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "models/bedrock/projectile/cluster_charge.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/bedrock/projectile/cluster_charge.png");

    public ClusterChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ClusterChargeEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ClusterChargeEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        var model = ProjectileModelReloadListener.INSTANCE.getModel(MODEL);
        if (model == null) return;

        poseStack.pushPose();

        Direction direction = entity.getDirection();
        if (direction == Direction.UP) {
            // 地板放置：圆盘平贴地面，出弹管朝下（翻转修正，不再底朝天）
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        } else if (direction == Direction.DOWN) {
            // 天花板（当前不可放置，防御性处理：圆盘平贴、出弹管朝上）
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(180f));
            poseStack.mulPose(Axis.YN.rotationDegrees(direction.toYRot() + 180f));

            if (direction == Direction.EAST || direction == Direction.WEST) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            }
        }

        if (!entity.isFacingLeft()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        }

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
