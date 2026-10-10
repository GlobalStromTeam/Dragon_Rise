package com.redabysslucia.dragonrise_reforge.client.renderer.ammo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.client.model.ammo.MissileShellModel;
import com.redabysslucia.dragonrise_reforge.entities.projectile.MissileShellEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 空发射筒渲染：朝向飞行方向，并随存活时间缓慢翻滚（翻滚在客户端由 tickCount 推导，避免额外的同步开销）。
 */
public class MissileShellRenderer extends GeoEntityRenderer<MissileShellEntity> {

    public MissileShellRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MissileShellModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MissileShellEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        Vec3 motion = entity.getDeltaMovement();
        float yaw;
        float pitch;
        if (motion.lengthSqr() > 1.0E-4) {
            yaw = (float) (Mth.atan2(motion.x, motion.z) * (180F / (float) java.lang.Math.PI));
            pitch = (float) (-Mth.atan2(motion.y, motion.horizontalDistance()) * (180F / (float) java.lang.Math.PI));
        } else {
            yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
            pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        }

        float roll = (entity.tickCount + partialTicks) * 6.0F;   // 缓慢翻滚，纯客户端表现

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F + pitch));
        poseStack.mulPose(Axis.XP.rotationDegrees(roll));
        super.render(entity, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
        poseStack.popPose();
    }
}
