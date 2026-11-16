package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.MissileLaunchGate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = VehicleEntity.class, remap = false)
public class VehicleMissileLaunchGateMixin {
    @Inject(method = "vehicleShoot(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"), cancellable = true)
    private void dragonrise$named(LivingEntity shooter, String weapon, Vec3 target, CallbackInfo ci) {
        if (!MissileLaunchGate.tryNamed((VehicleEntity) (Object) this, weapon)) ci.cancel();
    }

    @Inject(method = "vehicleShoot(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;Ljava/util/UUID;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"), cancellable = true)
    private void dragonrise$namedTarget(LivingEntity shooter, String weapon, UUID uuid, Vec3 target, CallbackInfo ci) {
        if (!MissileLaunchGate.tryNamed((VehicleEntity) (Object) this, weapon)) ci.cancel();
    }

    @Inject(method = "vehicleShoot(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/UUID;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"), cancellable = true)
    private void dragonrise$selected(LivingEntity shooter, UUID uuid, Vec3 target, CallbackInfo ci) {
        if (!MissileLaunchGate.trySelected((VehicleEntity) (Object) this, shooter)) ci.cancel();
    }
}
