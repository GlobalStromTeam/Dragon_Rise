package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
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
        // 用帧间插值位置（无人车客户端自维护 prev/cur 位置），消除 20Hz 同步跳变造成的移动卡顿
        Vec3 dronePos = drone.getRenderPosition(partialTicks);
        if (cameraType == CameraType.FIRST_PERSON || cameraType == CameraType.THIRD_PERSON_BACK) {
            // 第一人称式：相机在无人车上，沿视角方向小偏移（前方 0.18、上方 0.075）
            Vec3 look = player.getLookAngle();
            Vec3 camPos = dronePos.add(look.scale(0.18)).add(0.0, 0.075, 0.0);
            setPosition(camPos.x, camPos.y, camPos.z);
        } else {
            // 第三人称（F5 切到 THIRD_PERSON_FRONT）：相机在无人车后方追尾
            Vec3 look = player.getLookAngle();
            Vec3 camPos = dronePos.add(look.scale(-2.5)).add(0.0, 0.6, 0.0);
            setPosition(camPos.x, camPos.y, camPos.z);
        }

        ci.cancel();
    }
}
