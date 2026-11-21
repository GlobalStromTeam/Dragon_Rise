package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.SantanaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 大众桑塔纳渲染器：驱动方向盘骨骼 {@code move_control}。
 * <p>
 * SBW 里没有"方向盘"专用骨骼名，通用做法是自己写渲染器（参照 {@code A10Renderer} 对
 * {@code move_wingLR} 等骨骼的处理）：在 {@code transformCustomModelPart} 里取骨骼并叠加旋转。
 * <p>
 * 方向盘轴向：模型里轮圈由 11 段"薄 Y + 绕 Z 旋转"的方块组成，轮盘位于 XY 平面，
 * 因此旋转轴是 <b>局部 Z</b>（{@code rotateZ}）。
 * <p>
 * <b>转角来源</b>：只用 {@code rudderRot}。实体侧已经做过"速度敏感转向"
 * （车速越高舵角上限越小、转向动作越慢，见 {@link SantanaEntity#getSteerMaxAngle()}），
 * 而前轮 Turn 骨骼在 {@code GeoVehicleRenderer} 里同样读 {@code rudderRot} ——
 * 所以方向盘与前轮会一起变小、变慢，不需要在这里再乘额外系数。
 * <p>
 * 原有的漂移视觉（车身侧倾 / 反打方向）已随漂移功能一起停用。
 */
public class SantanaRenderer extends GeoVehicleRenderer<SantanaEntity> {

    /** 方向盘转角相对舵角的放大倍数（满舵约 2.5 rad ≈ 143°）。 */
    private static final float STEER_WHEEL_RATIO = 360f;

    /** 转向方向若与视觉相反，把它改成 -1。 */
    private static final float STEER_WHEEL_SIGN = 1.0f;

    /** 「点灯」时叠加的自发光贴图（V 键开关驱动，见 SantanaEntity#dr$isLightOn()）。 */
    private static final ResourceLocation LIGHT_GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/entity/glow/santana_glow.png");

    /**
     * 发光贴图：点灯时返回 {@code textures/entity/glow/santana_glow.png}，关灯走父类（返回 null）。
     * <p>superb 的 {@code renderEmissive} 拿到 null 就直接跳过，所以关灯时不会有额外绘制。
     */
    @Override
    public ResourceLocation getEmissiveTextureLocation(PoseStack poseStack, SantanaEntity entity) {
        if (entity.dr$isLightOn()) {
            return LIGHT_GLOW_TEXTURE;
        }
        return super.getEmissiveTextureLocation(poseStack, entity);
    }

    public SantanaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(SantanaEntity entity, VehicleModelInstance instance, PoseStack poseStack,
                                         float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        // ---- 漂移视觉（已停用，留档） ----
        // boolean drifting = entity.isDrifting();
        // float lean = drifting ? entity.getDriftLean(partialTicks) : 0f;
        // float slip = drifting ? entity.getSlipAngle(partialTicks) : 0f;
        // var root = instance.getBone("Root");
        // if (root != null && Math.abs(lean) > 0.01f) {
        //     root.rotation.rotateZ(Mth.clamp(lean, -MAX_DRIFT_LEAN, MAX_DRIFT_LEAN) * Mth.DEG_TO_RAD);
        // }
        // 漂移中反打方向：
        // if (drifting) {
        //     float counter = Mth.clamp(slip / MAX_SLIP_FOR_COUNTER, -1f, 1f);
        //     steer = steer * 0.75f + counter * 0.45f;
        // }

        var wheel = instance.getBone("move_control");
        if (wheel == null) {
            return;
        }

        // rudderRot 已由实体按车速限幅 / 限速，这里直接取用（插值保证 60fps 顺滑）
        float steer = Mth.lerp(partialTicks, entity.getRudderRotO(), entity.getRudderRot());
        wheel.rotation.rotateZ(steer * STEER_WHEEL_RATIO * STEER_WHEEL_SIGN * Mth.DEG_TO_RAD);
    }
}
