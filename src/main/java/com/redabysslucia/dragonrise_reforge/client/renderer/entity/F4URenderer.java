package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.F4UEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * F4U 海盗式战斗机渲染：螺旋桨动画状态机（参照卓越前线的斯图卡 {@code Ju87Renderer}）。
 * <p>
 * 状态机本体在卓越前线：F4U 的 {@code EngineType = Aircraft}，其引擎逻辑每 tick 执行
 * {@code propellerRot += 30 * power}（{@code VehicleEngineUtils}），并写入同步字段 {@code PROPELLER_ROT}；
 * {@code propellerRotO} 保存上一 tick 的值用于插值。引擎启动、加速、怠速、熄火后的逐渐停转
 * 全部由这套逻辑驱动，渲染器只需把角度套到 {@code move_propeller} 骨骼上——这正是之前缺的一步。
 * <p>
 * 与斯图卡一致：绕 Z 轴旋转、角度取负（模型手性相同）。
 * <p>
 * 与斯图卡不同的是：即使是残骸（isWreck）也照常应用角度，这样被打坏后螺旋桨是"停住"而不是"瞬间归零"。
 */
public class F4URenderer extends GeoVehicleRenderer<F4UEntity> {

    public F4URenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(F4UEntity entity, VehicleModelInstance instance, PoseStack poseStack,
                                        float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        var propeller = instance.getBone("move_propeller");
        if (propeller != null) {
            // 与斯图卡同款：-lerp(上一 tick 角度, 当前角度)，转速变化因此是平滑的
            propeller.rotation.rotateZ(-Mth.lerp(partialTicks, entity.getPropellerRotO(), entity.getPropellerRot()));
        }
    }
}
