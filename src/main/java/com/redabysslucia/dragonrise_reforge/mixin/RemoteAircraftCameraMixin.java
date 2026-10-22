package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.entities.ShuangweixieEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 遥控双尾蝎时把相机放到<b>当前席位的机位</b>（机头光电球 / 机背 / 机腹 / 尾撑上方）。
 * <p>
 * SBW 的遥控相机（{@code mixins/CameraMixin}）对"第一人称"和"第三人称背面"用的是<b>写死的小偏移</b>
 * （无人机原点前方 0.18 格、上方 0.075 格）—— 那是为巴掌大的四轴写的；
 * 双尾蝎机身长约 11.5 格，视点落在机身内部，画面上就是一片机体内壁，
 * 也因此之前只能靠渲染器把本机模型藏掉（藏了以后第三人称也看不到飞机）。
 * <p>
 * 这里改用载具自己的座位相机链：{@code getCameraPosition(..., isFirstPerson = true)}
 * → 座位 JSON 的 {@code CameraPos.Position}（与 SBW 有人驾驶时第一人称用的是同一条链）。
 * 相机机位都在机身外形之外，因此<b>模型可以正常渲染</b>；
 * 第三人称正面（F5）仍然交给官方的座位/追尾相机，不受影响。
 * <p>
 * <b>注入顺序</b>：和 SBW 的 CameraMixin 注入同一处，必须比它晚执行才能覆盖它的值，
 * 所以本 mixin 用 {@code @Mixin(priority = 1500)}（默认 1000，越大越晚应用、越晚执行）。
 */
@Mixin(value = Camera.class, priority = 1500)
public abstract class RemoteAircraftCameraMixin {

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    protected abstract void setPosition(double x, double y, double z);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0),
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            cancellable = true)
    private void dragonrise$remoteSeatCamera(BlockGetter level, Entity entity, boolean detached, boolean mirrored,
                                             float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean("Using") || !tag.getBoolean("Linked")) {
            return;
        }
        var drone = EntityFindUtil.findDrone(player.level(), tag.getString("LinkedDrone"));
        if (!(drone instanceof ShuangweixieEntity aircraft)) {
            return;
        }

        // 只有"机身内"这两档需要改写；第三人称正面走官方座位相机（机外追尾视角）
        CameraType type = mc.options.getCameraType();
        if (type != CameraType.FIRST_PERSON && type != CameraType.THIRD_PERSON_BACK) {
            return;
        }

        Vec3 seatCam = aircraft.getCameraPosition(partialTicks, player, false, true);
        if (seatCam == null) {
            return;
        }

        // 视角朝向仍由载具姿态决定（与官方一致），只是把机位换到座位机位
        setRotation(aircraft.getYaw(partialTicks), aircraft.getPitch(partialTicks));
        setPosition(seatCam.x, seatCam.y, seatCam.z);
        ci.cancel();
    }
}
