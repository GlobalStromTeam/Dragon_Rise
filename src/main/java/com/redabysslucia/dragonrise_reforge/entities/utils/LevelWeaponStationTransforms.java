package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector3d;

/** Weapon stations mounted below the model's inverse turret-pitch compensation bone. */
public final class LevelWeaponStationTransforms {
    private LevelWeaponStationTransforms() {
    }

    public static Matrix4d getGunTransform(VehicleEntity entity, float partialTicks) {
        Matrix4d transform = entity.getTurretTransform(partialTicks);
        transform.rotate(Axis.XP.rotationDegrees(-entity.getTurretCustomPitch()));
        var position = entity.getPassengerWeaponStationPosition();
        transform.translate(position.x, position.y, position.z);
        float yaw = Mth.lerp(partialTicks, entity.getGunYRotO(), entity.getGunYRot())
                - Mth.lerp(partialTicks, entity.getTurretYRotO(), entity.getTurretYRot());
        transform.rotate(Axis.YP.rotationDegrees(yaw));
        return transform;
    }

    public static Matrix4d getBarrelTransform(VehicleEntity entity, float partialTicks) {
        Matrix4d transform = entity.getGunTransform(partialTicks);
        var position = entity.getPassengerWeaponStationBarrelPosition();
        transform.translate(position.x, position.y, position.z);
        transform.rotate(Axis.XP.rotationDegrees(
                Mth.lerp(partialTicks, entity.getGunXRotO(), entity.getGunXRot())));
        return transform;
    }

    public static Vec3 getDirection(VehicleEntity entity, float partialTicks) {
        Vector3d direction = entity.getPassengerWeaponStationBarrelTransform(partialTicks)
                .transformDirection(new Vector3d(0, 0, 1));
        return new Vec3(direction.x, direction.y, direction.z);
    }
}
