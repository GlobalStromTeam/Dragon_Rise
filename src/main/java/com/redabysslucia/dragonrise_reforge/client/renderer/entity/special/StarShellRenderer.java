package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redabysslucia.dragonrise_reforge.entities.special.StarShellEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * 照明弹渲染器：始终面向玩家的多层「亮白光球」（自绘径向渐变圆贴图，emissive 无视光照）。
 * 飞行时小而亮；悬停时放大并带轻微呼吸脉动，模拟大范围光源的视觉本体。
 * 三层（内芯 / 中层 / 外晕）叠加出柔和光晕，避免单层圆斑的生硬边缘。
 */
public class StarShellRenderer extends EntityRenderer<StarShellEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/entity/star_shell.png");

    public StarShellRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(StarShellEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(StarShellEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 始终面向玩家（billboard）
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        float hover = entity.isHovering() ? 1.0f : 0.0f;
        float pulse = 1.0f + 0.05f * Mth.sin((entity.tickCount + partialTick) * 0.12f); // 呼吸
        float base = Mth.lerp(hover, 0.22f, 0.4f) * pulse;
        float scale = Mth.lerp(hover, 1.0f, 1.25f);

        // 由外到内绘制（半透明混合顺序：先大后小，内芯最后画最实）
        drawLayer(buffer, poseStack, base * 3.60f * scale, 0.16f);
        drawLayer(buffer, poseStack, base * 2.10f * scale, 0.55f);
        drawLayer(buffer, poseStack, base * 1.00f * scale, 1.00f);

        poseStack.popPose();
    }

    private void drawLayer(MultiBufferSource buffer, PoseStack poseStack, float size, float alpha) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE, false));
        Matrix4f mat = poseStack.last().pose();
        float h = size * 0.5f;

        // 1.21.1 的 VertexConsumer 改为语句式：addVertex/setColor/setUv/setOverlay/setLight/setNormal，
        // 且 endVertex() 已移除（1.20.1 的链式 vertex(...).color(...).uv(...) 写法不可用）。
        addQuadVertex(vc, mat, -h, -h, 0.0f, 1f, 1f, 1f, alpha, 0.0f, 1.0f);
        addQuadVertex(vc, mat, h, -h, 0.0f, 1f, 1f, 1f, alpha, 1.0f, 1.0f);
        addQuadVertex(vc, mat, h, h, 0.0f, 1f, 1f, 1f, alpha, 1.0f, 0.0f);
        addQuadVertex(vc, mat, -h, h, 0.0f, 1f, 1f, 1f, alpha, 0.0f, 0.0f);
    }

    /** 以固定朝向（+Z）写入一个全亮、无 overlay 的四边形顶点。 */
    private static void addQuadVertex(VertexConsumer vc, Matrix4f mat,
                                      float x, float y, float z,
                                      float r, float g, float b, float a,
                                      float u, float v) {
        vc.addVertex(mat, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0f, 0.0f, 1.0f);
    }
}
