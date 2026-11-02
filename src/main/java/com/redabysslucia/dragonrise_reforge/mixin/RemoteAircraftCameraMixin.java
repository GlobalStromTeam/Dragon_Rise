package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.entities.AbstractRemotePilotDrone;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 遥控本模组无人机时，把相机完全交给载具：机位取座位数据、朝向取实体的
 * {@code dr$cameraYaw / dr$cameraPitch}（机型可覆写，例如让视线跟着机腹炮管）。
 * <p>
 * <b>为什么必须注入 {@code setup} 的 HEAD</b>（而不是和 SBW 抢 {@code setRotation} 那个注入点）：
 * 卓越前线的 {@code mixins/CameraMixin} 在同一个 {@code setRotation} 调用点注入，而且
 * <b>直接 {@code info.cancel()}</b>。两个 mixin 抢同一个点时，谁的回调排在前面谁说了算 ——
 * SBW 先 cancel，我们的整个回调就被跳过。表现就是：遥控视角的俯仰跟机体绑死、
 * 机位落在机身内部，炮管一压就穿模。HEAD 是最靠前的注入点，永远先执行，
 * 这里接管后 SBW 那条分支不会再有执行机会。
 * <p>
 * <b>代价</b>：{@code ci.cancel()} 会跳过 {@code setup} 余下的 vanilla 逻辑，所以这里
 * 手动复刻它的前半段（{@code initialized / level / entity / detached} 四个字段）。
 * vanilla 之后只剩"detached 时后拉 getMaxZoom / 睡觉时抬高 0.3"，
 * 对"机身内视角"这两档本来就不该执行（SBW 自己 cancel 时同样跳过）。
 */
@Mixin(value = Camera.class, priority = 1500)
public abstract class RemoteAircraftCameraMixin {

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    protected abstract void setPosition(double x, double y, double z);

    /** vanilla {@code Camera.setup} 的前四个字段赋值 —— cancel 会跳过它们，这里补齐 */
    @Shadow
    private boolean initialized;
    @Shadow
    private BlockGetter level;
    @Shadow
    private Entity entity;
    @Shadow
    private boolean detached;

    @Inject(at = @At("HEAD"), cancellable = true,
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V")
    private void dragonrise$remoteSeatCamera(BlockGetter pLevel, Entity pEntity, boolean pDetached,
                                             boolean pMirrored, float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        // 一次 tick 内缓存过，相机 setup 每帧都会调，不要每次扫实体表
        AbstractRemotePilotDrone aircraft = RemotePilotHelper.remoteAircraft(player);
        if (aircraft == null) {
            return;
        }

        // 只有"机身内"这两档需要接管；第三人称正面走官方座位/追尾相机
        CameraType type = mc.options.getCameraType();
        if (type != CameraType.FIRST_PERSON && type != CameraType.THIRD_PERSON_BACK) {
            return;
        }

        Vec3 cam = aircraft.getCameraPosition(partialTicks, player, false, true);
        if (cam == null) {
            return;
        }

        // —— 复刻 vanilla setup 的前半段（cancel 之后没人补）——
        this.initialized = true;
        this.level = pLevel;
        this.entity = pEntity;
        this.detached = pDetached;

        setRotation(aircraft.dr$cameraYaw(partialTicks), aircraft.dr$cameraPitch(partialTicks));
        setPosition(cam.x, cam.y, cam.z);
        ci.cancel();
    }
}
