package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class WLHGZU23Entity extends SyncCameraVehicle {
    public WLHGZU23Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private final Float[][] PitchAdjustments = {
            {0f, 36f, 23f, 0f, -10f},
            {0f, -36f, 23f, 0f, -10f},
    };


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