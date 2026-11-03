package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.LevelWeaponStationTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;

@SuppressWarnings("removal")
public class VT4A1Entity extends VehicleEntity {
    public VT4A1Entity(EntityType<?> pEntityType, Level pLevel) {
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
