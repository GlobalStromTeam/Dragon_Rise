package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.projectile.JavelinMissileEntity;
import com.atsuishio.superbwarfare.entity.projectile.Ru9m336MissileEntity;
import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.GroundMissileGuidance;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WireGuideMissileEntity.class, Ru9m336MissileEntity.class, JavelinMissileEntity.class})
public class GroundMissileTurnMixin {
    @Unique private GroundMissileGuidance.BeforeTick dragonrise$beforeGuidance;

    @Inject(method = "tick", at = @At("HEAD"))
    private void dragonrise$captureMotion(CallbackInfo ci) {
        dragonrise$beforeGuidance = GroundMissileGuidance.capture((Entity) (Object) this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void dragonrise$limitTurn(CallbackInfo ci) {
        if (dragonrise$beforeGuidance != null) {
            GroundMissileGuidance.finishTick((Entity) (Object) this,
                    dragonrise$beforeGuidance.motion(), dragonrise$beforeGuidance.look());
        }
    }
}
