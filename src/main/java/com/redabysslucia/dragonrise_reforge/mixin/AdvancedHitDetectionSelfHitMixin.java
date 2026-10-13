package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 弹射物<b>弹道扫描</b>阶段的自伤豁免（{@code IAdvancedHitDetection.findEntitiesOnPath}）。
 * <p>
 * 官方在这条扫描里过滤掉"射手所在载具"用的是：
 * <pre>entity == this.owner!!.vehicle || entity.getRootVehicle() === this.owner!!.getRootVehicle()</pre>
 * 遥控者 {@code owner.vehicle} 为 null，于是飞机被当成普通实体进入命中列表 —— 弹丸在半路就被自己的飞机拦下。
 * 这里把该处的 {@code owner.vehicle} 取值改成"遥控中的飞机"，
 * 第一段比较即成立，飞机被正确豁免（第二段的 root vehicle 比较保持原样）。
 * <p>
 * 该默认方法为多个弹射物类共用（{@code FastThrowableProjectile} 等），改这一处覆盖面最广。
 * <b>remap</b>：注入目标是原版 {@code Entity.getVehicle()}，需走 refmap（默认 remap = true）。
 */
@Mixin(targets = "com.atsuishio.superbwarfare.entity.projectile.IAdvancedHitDetection$DefaultImpls")
public class AdvancedHitDetectionSelfHitMixin {

    @Redirect(method = "findEntitiesOnPath", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/Entity;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private static Entity dragonrise$ownerVehicleAsRemoteAircraft(Entity owner) {
        Entity ridden = owner.getVehicle();
        if (ridden != null) {
            return ridden;
        }
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            var aircraft = RemotePilotHelper.remoteAircraft(player);
            if (aircraft != null) {
                return aircraft;
            }
        }
        return null;
    }
}
