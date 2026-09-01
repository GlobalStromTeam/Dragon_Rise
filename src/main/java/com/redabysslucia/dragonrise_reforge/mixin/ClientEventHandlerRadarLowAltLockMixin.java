package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ProjectileInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * 雷达弹低空锁定限制：任何情况下雷达弹（aim120/pl_12/pl_15/r_77）都无法锁定
 * 离地面 25 格以内的实体。在 vehicleWeaponSeeking 结束后，若当前武器是雷达弹
 * 且最近候选目标离地高度低于 25 格，则清除该候选（无法锁定）。
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEventHandlerRadarLowAltLockMixin {

    /** 雷达弹实体 id 集合（本包所有雷达制导空空弹） */
    private static final Set<String> RADAR_MISSILES = Set.of(
            "dragonrise_reforge:aim120",
            "dragonrise_reforge:pl_12",
            "dragonrise_reforge:pl_15",
            "dragonrise_reforge:r_77"
    );

    /** 低空保护高度（格）：离地低于此值无法锁定 */
    private static final double MIN_LOCK_HEIGHT = 25.0;

    @Inject(method = "vehicleWeaponSeeking", at = @At("RETURN"))
    private void dragonrise$filterLowAltRadarTarget(Player player, CallbackInfo ci) {
        Entity candidate = ClientEventHandler.nearestEntityVehicle;
        if (candidate == null) return;

        // 确认当前武器是雷达弹
        if (player == null || player.getVehicle() == null) return;
        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) return;
        GunData data = vehicle.getGunData(player);
        if (data == null) return;

        Object projectile = data.get(GunProp.PROJECTILE);
        if (!(projectile instanceof ProjectileInfo info)) return;
        if (!RADAR_MISSILES.contains(info.getItemId())) return;

        // 计算候选目标离地高度（WORLD_SURFACE 高度图）
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        int surfaceY = level.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                candidate.blockPosition().getX(),
                candidate.blockPosition().getZ()
        );
        double heightAboveGround = candidate.getY() - surfaceY;

        if (heightAboveGround < MIN_LOCK_HEIGHT) {
            ClientEventHandler.nearestEntityVehicle = null;
        }
    }
}
