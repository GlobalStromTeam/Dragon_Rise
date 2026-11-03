package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.SilverhandMkIvEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * 银手 - Mk. IV 渲染器。
 * 车体副炮使用原生武器站骨骼；偏航相对车体，与实体的弹药生成变换一致。
 */
public class SilverhandMkIvRenderer extends AnimatedTrackedVehicleRenderer<SilverhandMkIvEntity> {
    public SilverhandMkIvRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, "silverhand_mk_iv");
    }

    @Override
    public void transformCustomModelPart(SilverhandMkIvEntity entity, VehicleModelInstance instance,
                                         PoseStack poseStack, float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
        var yaw = instance.getBone("passengerWeaponStationYaw");
        if (yaw != null) {
            yaw.rotation.rotationY(Mth.lerp(partialTicks,
                    entity.getGunYRotO(), entity.getGunYRot()) * Mth.DEG_TO_RAD);
        }
    }
}
