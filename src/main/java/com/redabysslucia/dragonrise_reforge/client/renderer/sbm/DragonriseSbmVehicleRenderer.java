package com.redabysslucia.dragonrise_reforge.client.renderer.sbm;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.client.model.sbm.DragonriseModelReloadListener;
import com.redabysslucia.dragonrise_reforge.client.model.sbm.DragonriseModelReloadListener.CachedVehicleModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
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

    protected final DragonriseModelReloadListener modelListener;

    public DragonriseSbmVehicleRenderer(EntityRendererProvider.Context manager, DragonriseModelReloadListener modelListener) {
        super(manager);
        this.modelListener = modelListener;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        String[] parts = entity.getType().getDescriptionId().split("\\.");
        return new ResourceLocation(parts[1], "textures/bedrock/vehicle/" + parts[2] + ".png");
    }

    public ResourceLocation getEmissiveTextureLocation(T entity) {
        return null;
    }

    public ResourceLocation getModelLocation(T entity) {
        String[] parts = entity.getType().getDescriptionId().split("\\.");
        return new ResourceLocation(parts[1], parts[2]);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var cached = modelListener.getCachedModel(getModelLocation(entity));
        if (cached == null) return;

        var model = cached.model;
        var texture = getTextureLocation(entity);
        var emissiveTexture = getEmissiveTextureLocation(entity);

        poseStack.pushPose();

        rotateVehicleAxis(entity, poseStack, entityYaw, partialTick);

        cached.resetBones();

        tickVariables(entity, entityYaw, partialTick);
        transformCustomModelPart(entity, cached, poseStack, entityYaw, partialTick);

        model.renderToBuffer(
                poseStack, buffer,
                RenderType.entityCutout(texture),
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

        renderCustomPart(entity, cached, poseStack, entityYaw, partialTick, buffer, packedLight);

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

        var turret = cached.getBone("turret");
        if (turret != null) {
            turret.rotation.rotationY(turretYRot * Mth.DEG_TO_RAD);
        }

        var barrel = cached.getBone("barrel");
        if (barrel != null) {
            float rot = Mth.clamp(-turretXRot, vehicle.getTurretMinPitch(), vehicle.getTurretMaxPitch()) * Mth.DEG_TO_RAD;
            barrel.rotation.rotationX(rot);
        }

        var laser = cached.getBone("laser");
        if (laser != null) {
            laser.zScale = 10 * vehicle.getLaserLength();
            float scale = Mth.clamp(Mth.lerp(partialTicks, vehicle.getLaserScaleO(), vehicle.getLaserScale()), 0f, 1.2f);
            laser.xScale = scale;
            laser.yScale = scale;
        }
    }

    protected void renderCustomPart(T vehicle, CachedVehicleModel cached, PoseStack poseStack, float entityYaw, float partialTicks, MultiBufferSource buffer, int packedLight) {
    }

    protected void rotateVehicleAxis(T entityIn, PoseStack poseStack, float entityYaw, float partialTicks) {
        var root = new Vec3(0.0, entityIn.getRotateOffsetHeight(), 0.0);
        poseStack.rotateAround(Axis.YP.rotationDegrees(-entityYaw + 180), (float) root.x, (float) root.y, (float) root.z);
        poseStack.rotateAround(Axis.XP.rotationDegrees(-Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot())), (float) root.x, (float) root.y, (float) root.z);
        poseStack.rotateAround(Axis.ZP.rotationDegrees(-Mth.lerp(partialTicks, entityIn.getPrevRoll(), entityIn.getRoll())), (float) root.x, (float) root.y, (float) root.z);
    }
}
