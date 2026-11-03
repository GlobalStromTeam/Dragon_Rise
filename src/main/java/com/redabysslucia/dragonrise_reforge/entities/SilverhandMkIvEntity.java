package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector3d;

/**
 * Silverhand Mk IV 重型坦克。
 * <p>
 * 座位1 = 驾驶员兼主炮手，主炮使用 turret/barrel 骨骼；
 * 座位2 = 车体副炮手，车体炮使用独立的 passengerWeaponStationYaw/Pitch 骨骼。
 */
@SuppressWarnings("removal")
public class SilverhandMkIvEntity extends DragonriseVehicleBase {

    public SilverhandMkIvEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public Matrix4d getGunTransform(float partialTicks) {
        Matrix4d transform = getVehicleTransform(partialTicks);
        var position = getPassengerWeaponStationPosition();
        transform.translate(position.x, position.y, position.z);
        transform.rotate(Axis.YP.rotationDegrees(
                Mth.lerp(partialTicks, getGunYRotO(), getGunYRot())));
        return transform;
    }

    @Override
    public Matrix4d getPassengerWeaponStationBarrelTransform(float partialTicks) {
        Matrix4d transform = getGunTransform(partialTicks);
        var position = getPassengerWeaponStationBarrelPosition();
        transform.translate(position.x, position.y, position.z);
        transform.rotate(Axis.XP.rotationDegrees(
                Mth.lerp(partialTicks, getGunXRotO(), getGunXRot())));
        return transform;
    }

    @Override
    public Vec3 getPassengerWeaponStationVector(float partialTicks) {
        Vector3d direction = getPassengerWeaponStationBarrelTransform(partialTicks)
                .transformDirection(new Vector3d(0, 0, 1));
        return new Vec3(direction.x, direction.y, direction.z);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 140;
    }

    @Override
    public float getWheelMaxHealth() {
        return 110;
    }

    @Override
    public float getEngineMaxHealth() {
        return 140;
    }
}
