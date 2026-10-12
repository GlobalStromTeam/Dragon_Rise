package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 弹射物自伤豁免：让遥控驾驶的飞机享受和"骑乘载具"一样的待遇。
 * <p>
 * 官方 {@code FastThrowableProjectile.onHit} 里判定的是：
 * <pre>if (this.owner is Player &amp;&amp; entity.hasIndirectPassenger(this.owner)) return</pre>
 * 即"被打中的实体里坐着射手本人 → 跳过"。遥控驾驶时射手不在飞机上，这条不成立，
 * 于是自己打出的机炮/火箭会命中自己的飞机（表现为弹丸一离膛就撞在自己身上）。
 * 这里补一条等价判断：被命中的实体若是该射手正在遥控的飞机，同样跳过。
 * <p>
 * 机炮（SmallCannonShellEntity）与火箭（SmallRocketEntity）都继承自这个基类，改这里一并覆盖。
 * <b>remap</b>：注入目标是原版方法 {@code hasIndirectPassenger}，必须走 refmap（默认 remap = true）。
 */
@Mixin(FastThrowableProjectile.class)
public class FastThrowableProjectileSelfHitMixin {

    @Redirect(method = "onHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hasIndirectPassenger(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean dragonrise$exemptRemoteAircraft(Entity hitEntity, Entity passenger) {
        return hitEntity.hasIndirectPassenger(passenger)
                || RemotePilotHelper.isRemoteControlledVehicle(hitEntity, passenger);
    }
}
