package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class NightVisionVehicle extends GeoVehicleEntity implements INightVisionVehicle {

    public static final EntityDataAccessor<Boolean> NV_Enable = SynchedEntityData.defineId(NightVisionVehicle.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NV_Enable, false);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(NV_Enable, compound.getBoolean("NV_Enable"));
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("NV_Enable", this.entityData.get(NV_Enable));
    }

    @Override
    public boolean getNVEnable() {
        return this.entityData.get(NV_Enable);
    }

    @Override
    public void setNVEnable(boolean enable) {
        this.entityData.set(NV_Enable, enable);
    }

    public NightVisionVehicle(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

}
