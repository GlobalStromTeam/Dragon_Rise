package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * 侦察无人车视角绑定 —— 实现方式参考 superbwarfare 的 CameraMixin：
 * 注入 Camera.setup，在玩家手持监控平板激活遥控无人车时，
 * 把相机旋转设为本机玩家的视角（原生即时、无平滑），相机位置设为无人车位置，
 * 并取消原版相机计算。
 */
@Mixin(Camera.class)
public abstract class R6DroneCameraMixin {

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    protected abstract void setPosition(double x, double y, double z);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0),
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            cancellable = true)
    private void dr$onSetup(BlockGetter level, Entity entity, boolean detached, boolean mirrored,
                            float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) return;

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean("Using") || !tag.getBoolean("Linked")) return;

        R6DroneEntity drone = R6DroneEntity.findDrone(player.level(), tag.getString("LinkedDrone"));
        if (drone == null) return;

        // 相机旋转 = 控制者玩家视角（原生即时，无 SBW 的平滑/滞后）
        setRotation(player.getYRot(), player.getXRot());

        CameraType cameraType = mc.options.getCameraType();
        // 帧间低通滤波位置（advanceSmoothPosition 每帧推进一次），
        // 吸收位置包到达节奏/增量编码精度的微小速度波动，消除移动顿挫。
        Vec3 dronePos = drone.advanceSmoothPosition(partialTicks);

        if (cameraType == CameraType.FIRST_PERSON || cameraType == CameraType.THIRD_PERSON_BACK) {
            // 第一人称式：相机基本在无人车中心（前方仅 0.05），垂直高度固定在地面上方 0.15。
            // 贴近方块时车头先碰墙，相机不会再嵌进方块（原 0.18 太靠前）。
            double yawRad = Math.toRadians(player.getYRot());
            Vec3 flatLook = new Vec3(-Mth.sin((float) yawRad), 0.0, Mth.cos((float) yawRad));
            Vec3 camPos = dronePos.add(flatLook.scale(0.05)).add(0.0, 0.15, 0.0);
            setPosition(camPos.x, camPos.y, camPos.z);
        } else {
            // 第三人称（F5 切到 THIRD_PERSON_FRONT）：相机在无人车后方追尾（三维视角方向，原样）
            Vec3 look = player.getLookAngle();
            Vec3 camPos = dronePos.add(look.scale(-2.5)).add(0.0, 0.6, 0.0);
            setPosition(camPos.x, camPos.y, camPos.z);
        }

        ci.cancel();
    }
}
