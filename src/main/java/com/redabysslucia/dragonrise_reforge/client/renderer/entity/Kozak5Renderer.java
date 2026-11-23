package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.Kozak5Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * 哥萨克-5 渲染器：驱动方向盘骨骼 {@code move_control}。
 * <p>
 * 模型里方向盘是 XY 平面上的薄片（size.z = 0），旋转轴取 <b>局部 Z</b>（{@code rotateZ}）；
 * 转角来自载具转向输入 {@code rudderRot}，与 SBW 驱动转向轮的取法一致。
 */
public class Kozak5Renderer extends GeoVehicleRenderer<Kozak5Entity> {

    /** 方向盘转角放大倍数（满舵约 2.5 rad ≈ 143°）。 */
    private static final float STEER_WHEEL_RATIO = 2.5f;

    /** 转向方向若与视觉相反，改成 -1。 */
    private static final float STEER_WHEEL_SIGN = 1.0f;

    public Kozak5Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(Kozak5Entity entity, VehicleModelInstance instance, PoseStack poseStack,
                                         float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        var wheel = instance.getBone("move_control");
        if (wheel == null) {
            return;
        }
        float steer = Mth.lerp(partialTicks, entity.getRudderRotO(), entity.getRudderRot());
        wheel.rotation.rotateZ(steer * STEER_WHEEL_RATIO * STEER_WHEEL_SIGN * Mth.DEG_TO_RAD);
    }
}
