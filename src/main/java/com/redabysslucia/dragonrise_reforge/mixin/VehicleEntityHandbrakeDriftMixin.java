package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.HandbrakeDriftVehicle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 手刹漂移物理的<b>兜底</b>注入点。
 *
 * <p>主线接入在 {@link HandbrakeDriftVehicle} 的子类里（覆写 {@code travel()}，
 * 按 driftBegin → super.travel → driftEnd 三步执行，见 {@code SantanaEntity}）。
 * 这里覆盖"子类没有覆写 travel，或忘了调 {@code super.travel()}"的情况。
 *
 * <p><b>为什么不会算两遍</b>：SBW 的 {@code travel()} 是 open 方法，
 * 子类覆写后调 {@code super.travel()} 时，本 mixin 注入的 {@code VehicleEntity.travel}
 * 也会被调用，于是 TAIL 会跑一次；子类自己收尾时再调 {@code driftEnd()} 就是第二次。
 * {@link HandbrakeDriftVehicle} 用两个闸门挡掉重复：
 * <ul>
 *   <li>{@code activeEntry}：子类 driftBegin 已登记 → mixin HEAD 不重复缓存速度；</li>
 *   <li>{@code lastDriftTick}：无论从哪条路进来，同一 {@code tickCount} 只施加一次物理。</li>
 * </ul>
 *
 * <p>只对 {@link HandbrakeDriftVehicle} 子类生效，对模组其余 149 台载具零影响。
 * 两个注入点都带 {@code require = 0}：SBW 若改签名，最坏结果是"没有漂移"，不会让游戏起不来。
 * （发布前把 require 临时改成 1 跑过一次 runClient：HEAD / TAIL 均与
 * {@code VehicleEntity.travel} 匹配成功，说明这条兜底链路是通的。）
 */
@Mixin(VehicleEntity.class)
public abstract class VehicleEntityHandbrakeDriftMixin {

    @Unique
    private HandbrakeDriftVehicle dragonrise$driftVehicle() {
        return (Object) this instanceof HandbrakeDriftVehicle vehicle ? vehicle : null;
    }

    /**
     * travel 之前：缓存进入本 tick 的速度（回正期要用它保住动量）。
     * 双端都记：客户端也在跑同一套动力学做预测，不记会让回正手感两端不一致。
     */
    @Inject(method = "travel", at = @At("HEAD"), require = 0)
    private void dragonrise$driftBegin(CallbackInfo ci) {
        HandbrakeDriftVehicle vehicle = dragonrise$driftVehicle();
        if (vehicle != null) {
            vehicle.driftBeginHook();
        }
    }

    /** travel 之后：执行本 tick 的漂移后处理（内部按 tickCount 去重，双端各算各的） */
    @Inject(method = "travel", at = @At("TAIL"), require = 0)
    private void dragonrise$driftEnd(CallbackInfo ci) {
        HandbrakeDriftVehicle vehicle = dragonrise$driftVehicle();
        if (vehicle == null) return;
        vehicle.driftEndHook();
    }
}
