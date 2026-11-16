package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.redabysslucia.dragonrise_reforge.entities.utils.GroundMissileGuidance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Capture the actual ammo supplier before spawning, including delayed and AI fire. */
@Mixin(value = GunItem.class, remap = false)
public class GroundMissileLaunchMixin {
    @Redirect(method = "shootBullet", at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean dragonrise$tagLaunch(ServerLevel level, Entity projectile, ShootParameters parameters) {
        return GroundMissileGuidance.spawn(level, projectile, parameters.ammoSupplier);
    }
}
