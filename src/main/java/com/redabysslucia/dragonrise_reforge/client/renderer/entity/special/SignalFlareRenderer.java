package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redabysslucia.dragonrise_reforge.entities.special.SignalFlareEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * 信号棒渲染器：2D 图标卡片（textures/item/signal_flare.png，与物品栏同图，不再用 3D 模型）。
 * 图标始终面向玩家（billboard，乘相机旋转，与烟花火箭同款做法）：
 * 飞行/落地/任意观察角度都完整可见，适合远处辨认。
 * 光照用实体的 packedLight（与雪球/箭/掉落物一致，随环境明暗变化，不强制满亮）。
 */
public class SignalFlareRenderer extends EntityRenderer<SignalFlareEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("dragonrise_reforge", "textures/item/signal_flare.png");

    /** 卡片边长（格）：碰撞盒 0.2，卡片 0.5 便于远处看清 */
    private static final float SIZE = 0.5f;

    /** 抬升量：实体坐标在脚底，卡片中心上移，落地时图标不埋进地面 */
    private static final float LIFT = 0.3f;

    public SignalFlareRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SignalFlareEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(SignalFlareEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0, LIFT, 0.0);
        // billboard：乘以相机旋转，卡片法线始终对准观察者
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        float h = SIZE * 0.5f;

        // 左下 → 右下 → 右上 → 左上（图像正立，双面 no-cull 渲染）
        vertexConsumer.vertex(mat, -h, -h, 0.0f).color(1f, 1f, 1f, 1f).uv(0.0f, 1.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(mat, h, -h, 0.0f).color(1f, 1f, 1f, 1f).uv(1.0f, 1.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(mat, h, h, 0.0f).color(1f, 1f, 1f, 1f).uv(1.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(mat, -h, h, 0.0f).color(1f, 1f, 1f, 1f).uv(0.0f, 0.0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 0.0f, 1.0f).endVertex();

        poseStack.popPose();
    }
}
