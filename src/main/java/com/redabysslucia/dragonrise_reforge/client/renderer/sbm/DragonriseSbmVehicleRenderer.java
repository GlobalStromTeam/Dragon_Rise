package com.redabysslucia.dragonrise_reforge.client.renderer.sbm;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import com.redabysslucia.dragonrise_reforge.init.DragonriseBedrockLoader;
import com.redabysslucia.dragonrise_reforge.init.DragonriseBedrockLoader.CachedVehicleModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaterniond;
import org.joml.Quaternionf;

public abstract class DragonriseSbmVehicleRenderer<T extends VehicleEntity> extends EntityRenderer<T> {

    protected float pitch = 0f;
    protected float yaw = 0f;
    protected float roll = 0f;
    protected float leftWheelRot = 0f;
    protected float rightWheelRot = 0f;
    protected float leftTrack = 0f;
    protected float rightTrack = 0f;
    protected float turretYRot = 0f;
    protected float turretXRot = 0f;
    protected float turretYaw = 0f;
    protected float recoilShake = 0f;

    public DragonriseSbmVehicleRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        String[] parts = entity.getType().getDescriptionId().split("\\.");
        return new ResourceLocation(parts[1], "textures/bedrock/vehicle/" + parts[2] + ".png");
    }

    public ResourceLocation getEmissiveTextureLocation(T entity) {
        return null;
    }

    public abstract ResourceLocation getModelLocation(T entity);

    public float renderScale() {
        return 1f;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var cached = DragonriseBedrockLoader.getCachedModel(getModelLocation(entity));
        if (cached == null) return;

        var model = cached.model;
        var texture = getTextureLocation(entity);
        var emissiveTexture = getEmissiveTextureLocation(entity);

        poseStack.pushPose();

        this.rotateVehicleAxis(entity, poseStack, entityYaw, partialTick);
        poseStack.scale(renderScale(), renderScale(), renderScale());

        model.applyPose(model.getBindPose());

        this.tickVariables(entity, entityYaw, partialTick);
        this.transformCustomModelPart(entity, cached, poseStack, entityYaw, partialTick);

        model.renderToBuffer(
                poseStack, buffer,
                RenderType.entityTranslucent(texture),
                BedrockModelRenderTypes.polyMeshCutout(texture),
                packedLight, OverlayTexture.NO_OVERLAY
        );

        if (emissiveTexture != null) {
            model.renderToBuffer(
                    poseStack, buffer,
                    RenderType.eyes(emissiveTexture),
                    BedrockModelRenderTypes.polyMeshCutout(emissiveTexture),
                    packedLight, OverlayTexture.NO_OVERLAY
            );
        }

        this.renderCustomPart(entity, cached, poseStack, entityYaw, partialTick, buffer, packedLight);

        poseStack.popPose();
    }

    protected void tickVariables(T vehicle, float entityYaw, float partialTicks) {
        pitch = vehicle.getPitch(partialTicks);
        yaw = vehicle.getYaw(partialTicks);
        roll = vehicle.getRoll(partialTicks);

        leftWheelRot = (float) Mth.lerp(partialTicks, vehicle.getLeftWheelRotO(), vehicle.getLeftWheelRot());
        rightWheelRot = (float) Mth.lerp(partialTicks, vehicle.getRightWheelRotO(), vehicle.getRightWheelRot());

        leftTrack = (float) Mth.lerp(partialTicks, vehicle.getLeftTrackO(), vehicle.getLeftTrack());
        rightTrack = (float) Mth.lerp(partialTicks, vehicle.getRightTrackO(), vehicle.getRightTrack());

        turretYRot = (float) Mth.lerp(partialTicks, vehicle.getTurretYRotO(), vehicle.getTurretYRot());
        turretXRot = (float) Mth.lerp(partialTicks, vehicle.getTurretXRotO(), vehicle.getTurretXRot());

        turretYaw = vehicle.getTurretYaw(partialTicks);

        recoilShake = (float) Mth.lerp(partialTicks, vehicle.getRecoilShakeO(), vehicle.getRecoilShake());
    }

    protected void transformCustomModelPart(T vehicle, CachedVehicleModel cached, PoseStack poseStack, float entityYaw, float partialTicks) {
        // 车轮
        for (var wheel : cached.leftWheels) {
            wheel.rotation.rotationX(1.5f * leftWheelRot);
        }
        for (var wheel : cached.rightWheels) {
            wheel.rotation.rotationX(1.5f * rightWheelRot);
        }
        for (var wheel : cached.leftWheelsTurn) {
            var yawRot = Axis.YP.rotation(Mth.lerp(partialTicks, vehicle.getRudderRotO(), vehicle.getRudderRot()));
            var pitchRot = Axis.XP.rotation(1.5f * leftWheelRot);
            var quaternion = new Quaterniond(yawRot).mul(new Quaterniond(pitchRot));
            wheel.rotation.mul(new Quaternionf(quaternion));
        }
        for (var wheel : cached.rightWheelsTurn) {
            var yawRot = Axis.YP.rotation(Mth.lerp(partialTicks, vehicle.getRudderRotO(), vehicle.getRudderRot()));
            var pitchRot = Axis.XP.rotation(1.5f * rightWheelRot);
            var quaternion = new Quaterniond(yawRot).mul(new Quaterniond(pitchRot));
            wheel.rotation.mul(new Quaternionf(quaternion));
        }

        // 履带移动
        for (int i = 0; i < cached.leftTrackMove.size(); i++) {
            var bone = cached.leftTrackMove.get(i);
            float t = wrap(leftTrack + getTrackDistance() * i, vehicle);
            bone.y += getBoneMoveY(t);
            bone.z += getBoneMoveZ(t);
        }
        for (int i = 0; i < cached.rightTrackMove.size(); i++) {
            var bone = cached.rightTrackMove.get(i);
            float t = wrap(rightTrack + getTrackDistance() * i, vehicle);
            bone.y += getBoneMoveY(t);
            bone.z += getBoneMoveZ(t);
        }

        // 履带旋转
        for (int i = 0; i < cached.leftTrackRot.size(); i++) {
            var bone = cached.leftTrackRot.get(i);
            float t = wrap(leftTrack + getTrackDistance() * i, vehicle);
            bone.rotation.rotationX(-getBoneRotX(t) * Mth.DEG_TO_RAD);
        }
        for (int i = 0; i < cached.rightTrackRot.size(); i++) {
            var bone = cached.rightTrackRot.get(i);
            float t = wrap(rightTrack + getTrackDistance() * i, vehicle);
            bone.rotation.rotationX(-getBoneRotX(t) * Mth.DEG_TO_RAD);
        }

        // 射击时带来的车体摇晃视觉效果
        var base = cached.getBone("base");
        if (base != null) {
            float a = vehicle.getYawWhileShoot();
            float r = (Mth.abs(a) - 90f) / 90f;

            float r2;
            if (Mth.abs(a) <= 90f) {
                r2 = a / 90f;
            } else if (a < 0) {
                r2 = -(180f + a) / 90f;
            } else {
                r2 = (180f - a) / 90f;
            }

            base.x = -r2 * recoilShake * 0.5f;
            base.z = r * recoilShake;

            var qPitch = Axis.XP.rotationDegrees(r * recoilShake);
            var qRoll = Axis.ZP.rotationDegrees(r2 * recoilShake);
            var quaternion = new Quaterniond(qPitch).mul(new Quaterniond(qRoll));
            base.rotation.mul(new Quaternionf(quaternion));
        }

        // 炮塔
        var turret = cached.getBone("turret");
        if (turret != null) {
            turret.rotation.rotationY(turretYRot * Mth.DEG_TO_RAD);
        }

        // 炮管
        var barrel = cached.getBone("barrel");
        if (barrel != null) {
            float rot = Mth.clamp(-turretXRot, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD;
            barrel.rotation.rotationX(rot);
        }

        // 激光
        var laser = cached.getBone("laser");
        if (laser != null) {
            laser.zScale = 10 * vehicle.getLaserLength();
            float scale = Mth.clamp(Mth.lerp(partialTicks, vehicle.getLaserScaleO(), vehicle.getLaserScale()), 0f, 1.2f);
            laser.xScale = scale;
            laser.yScale = scale;
        }
    }

    protected float getTrackDistance() {
        return 0.5f;
    }

    protected float wrap(float t, VehicleEntity vehicle) {
        return t / 4f;
    }

    protected float getBoneMoveY(float t) {
        return 0.01f * Mth.sin(t * Mth.PI * 2) * 4;
    }

    protected float getBoneMoveZ(float t) {
        return 0.01f * Mth.cos(t * Mth.PI * 2) * 4;
    }

    protected float getBoneRotX(float t) {
        return 360f * t;
    }

    protected void rotateVehicleAxis(T entity, PoseStack poseStack, float entityYaw, float partialTick) {
        var root = new Vec3(0.0, entity.getRotateOffsetHeight(), 0.0);
        poseStack.rotateAround(
                Axis.YP.rotationDegrees(-entityYaw + 180),
                (float) root.x, (float) root.y, (float) root.z
        );
        poseStack.rotateAround(
                Axis.XP.rotationDegrees((float) -Mth.lerp(partialTick, entity.xRotO, entity.getXRot())),
                (float) root.x, (float) root.y, (float) root.z
        );
        poseStack.rotateAround(
                Axis.ZP.rotationDegrees((float) -Mth.lerp(partialTick, entity.getPrevRoll(), entity.getRoll())),
                (float) root.x, (float) root.y, (float) root.z
        );
    }

    protected void renderCustomPart(T entity, CachedVehicleModel cached, PoseStack poseStack, float entityYaw, float partialTick, MultiBufferSource buffer, int packedLight) {
    }
}
