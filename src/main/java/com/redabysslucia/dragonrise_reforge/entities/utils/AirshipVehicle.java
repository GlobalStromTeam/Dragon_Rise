package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.DataLoader;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;

import com.redabysslucia.dragonrise_reforge.utils.AirshipInfo;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class AirshipVehicle extends GeoVehicleEntity {

    private EngineInfo engineCache;

    @Override
    public void travel() {
        var computed = computed();

        //var engineType = EngineType.AIRSHIP;

        if (this.engineCache == null) {
            var engineInfo = computed.getEngineInfo();
            try {
                this.engineCache = DataLoader.GSON.fromJson(engineInfo, AirshipInfo.class);
            } catch (Exception e) {
                Mod.LOGGER.error("Failed to parse engine info for vehicle {}, {}", this, e);
            }
        } else {
            this.engineCache.work(this);
        }
    }

    public AirshipVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        
    }

}
