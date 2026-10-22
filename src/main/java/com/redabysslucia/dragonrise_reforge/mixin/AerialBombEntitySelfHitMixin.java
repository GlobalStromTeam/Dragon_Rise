package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.projectile.AerialBombEntity;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 航空炸弹的自伤豁免（{@code AerialBombEntity.afterHitEntity}）。
 * <p>
 * 官方写法：{@code if (entity == owner || (owner != null && entity == owner.vehicle) || ...) return}
 * —— 命中"射手本人或射手所在载具"时不结算效果。遥控驾驶时 {@code owner.vehicle} 为 null，
 * 炸弹落到自己飞机上就会照常结算伤害/爆炸。
 * 这里把该处 {@code owner.vehicle} 改成"遥控中的飞机"，使第一段比较成立。
 * <p>
 * {@code Mk82Entity} 继承自本类，用户武器里的 mk_82 由此覆盖。
 * <b>remap</b>：注入目标是原版 {@code Entity.getVehicle()}，需走 refmap（默认 remap = true）。
 */
@Mixin(AerialBombEntity.class)
public class AerialBombEntitySelfHitMixin {

    @Redirect(method = "afterHitEntity", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/Entity;getVehicle()Lnet/minecraft/world/entity/Entity;"))
    private Entity dragonrise$ownerVehicleAsRemoteAircraft(Entity owner) {
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
