package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.redabysslucia.dragonrise_reforge.entities.utils.GroundMissileGuidance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FastThrowableProjectile.class, remap = false)
public class GroundMissileSpawnDataMixin {
    @Inject(method = "writeSpawnData", at = @At("RETURN"))
    private void dragonrise$writeProfile(FriendlyByteBuf buffer, CallbackInfo ci) {
        GroundMissileGuidance.writeSpawn((Entity) (Object) this, buffer);
    }

    @Inject(method = "readSpawnData", at = @At("RETURN"))
    private void dragonrise$readProfile(FriendlyByteBuf buffer, CallbackInfo ci) {
        GroundMissileGuidance.readSpawn((Entity) (Object) this, buffer);
    }
}
