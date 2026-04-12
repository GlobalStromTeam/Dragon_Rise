package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.FireLightVisionVehicle;
import com.redabysslucia.dragonrise_reforge.utils.PitchAdjustUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class WLHGZU23Entity extends FireLightVisionVehicle {
    public WLHGZU23Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private final Float[][] PitchAdjustments = {
            {0f, 36f, 23f, 0f, -10f},
            {0f, -36f, 23f, 0f, -10f},
    };

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

    @Override
    protected void clampRotation(Entity entity) {
        super.clampRotation(entity);

        int index = getSeatIndex(entity);
        var seats = computed().seats();
        if (index < 0 || index >= seats.size()) return;
        var seat = seats.get(index);

        if (seat.transform.equals("WeaponStation") && !seat.canRotateBody) {
            super.passengerPitchOnTurret(entity, seat.minPitch, seat.maxPitch);
            super.passengerYawOnTurret(entity, seat.minYaw, seat.maxYaw, seat.orientation, false);
        }
    }

    @Override
    public void passengerPitchOnTurret(Entity entity, float turretMinPitch, float turretMaxPitch) {
        PitchAdjustUtil.adjustedPassengerPitchOnTurret(entity, turretMinPitch, turretMaxPitch, this, PitchAdjustments);
    }
    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 80;
    }

    @Override
    public float getWheelMaxHealth() {
        return 60;
    }

    @Override
    public float getEngineMaxHealth() {
        return 100;
    }
}