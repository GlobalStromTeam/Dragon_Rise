package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.redabysslucia.dragonrise_reforge.client.ClientMissileLaunchGate;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientMissileLaunchGateMixin {
    @Inject(method = "clientShootVehicle", at = @At("HEAD"), cancellable = true)
    private void dragonrise$waitForLaunch(Player player, VehicleEntity vehicle, GunData gun, CallbackInfo ci) {
        if (!ClientMissileLaunchGate.tryBegin(vehicle, gun)) ci.cancel();
    }
}
