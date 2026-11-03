package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.redabysslucia.dragonrise_reforge.entities.utils.LevelWeaponStationTransforms;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;

public class ZTZ99AHEntity extends DragonriseVehicleBase {
    public ZTZ99AHEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public Matrix4d getGunTransform(float partialTicks) {
        return LevelWeaponStationTransforms.getGunTransform(this, partialTicks);
    }

    @Override
    public Matrix4d getPassengerWeaponStationBarrelTransform(float partialTicks) {
        return LevelWeaponStationTransforms.getBarrelTransform(this, partialTicks);
    }

    @Override
    public Vec3 getPassengerWeaponStationVector(float partialTicks) {
        return LevelWeaponStationTransforms.getDirection(this, partialTicks);
    }
@Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 100;
    }

    @Override
    public float getWheelMaxHealth() {
        return 100;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }
}
