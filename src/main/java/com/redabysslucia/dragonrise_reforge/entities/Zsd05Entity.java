package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Zsd05Entity extends DragonriseVehicleBase {

    public Zsd05Entity(EntityType<Zsd05Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected boolean isSplashDefaultOpen() {
        // The authored waveboards are deployed; splash_off folds them on land.
        return true;
    }

}
