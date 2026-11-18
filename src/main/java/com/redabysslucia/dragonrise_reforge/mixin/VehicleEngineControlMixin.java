package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entity.vehicle.engine.EngineControl;
import com.redabysslucia.dragonrise_reforge.entity.vehicle.engine.EngineControlled;
import com.redabysslucia.dragonrise_reforge.entity.vehicle.engine.IdleFuel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adapts FCP's engine hooks to Dragonrise vehicles with different base classes. */
@Mixin(VehicleEntity.class)
public abstract class VehicleEngineControlMixin implements EngineControlled {
    @Unique
    private static final EntityDataAccessor<Byte> DRAGONRISE_ENGINE_STATE =
            SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.BYTE);
    @Unique
    private double dragonrise$idleFuel;

    @Unique
    private VehicleEntity dragonrise$vehicle() {
        return (VehicleEntity) (Object) this;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void dragonrise$defineEngineState(CallbackInfo ci) {
        dragonrise$vehicle().getEntityData().define(DRAGONRISE_ENGINE_STATE, EngineControl.OFF);
    }

    @Override
    public byte dragonrise$getEngineState() {
        return dragonrise$vehicle().getEntityData().get(DRAGONRISE_ENGINE_STATE);
    }

    @Override
    public void dragonrise$setEngineState(byte state) {
        dragonrise$vehicle().getEntityData().set(DRAGONRISE_ENGINE_STATE, state);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void dragonrise$tickEngine(CallbackInfo ci) {
        var vehicle = dragonrise$vehicle();
        if (!EngineControl.appliesTo(vehicle)) return;
        EngineControl.tick(vehicle);
        dragonrise$idleFuel = IdleFuel.tick(vehicle, dragonrise$idleFuel);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void dragonrise$saveEngine(CompoundTag tag, CallbackInfo ci) {
        if (EngineControl.appliesTo(dragonrise$vehicle())) {
            tag.putBoolean("DragonriseEngineRunning", dragonrise$getEngineState() == EngineControl.RUNNING);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void dragonrise$loadEngine(CompoundTag tag, CallbackInfo ci) {
        if (EngineControl.appliesTo(dragonrise$vehicle())) {
            dragonrise$setEngineState(tag.getBoolean("DragonriseEngineRunning") ? EngineControl.RUNNING : EngineControl.OFF);
        }
    }

    @Inject(method = {"forwardInputDown", "backInputDown", "leftInputDown", "rightInputDown", "upInputDown", "downInputDown"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void dragonrise$gateDriverInput(CallbackInfoReturnable<Boolean> cir) {
        if (EngineControl.blocksInput(dragonrise$vehicle())) cir.setReturnValue(false);
    }

    @Inject(method = "getEngineSoundVolume", at = @At("HEAD"), cancellable = true, remap = false)
    private void dragonrise$muteNativeEngineSound(CallbackInfoReturnable<Float> cir) {
        if (EngineControl.usesSquadSound(dragonrise$vehicle())) cir.setReturnValue(0f);
    }
}
