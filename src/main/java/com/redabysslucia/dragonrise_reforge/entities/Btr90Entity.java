package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.Btr90ModelTransforms;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class Btr90Entity extends VehicleEntity {
    public Btr90Entity(EntityType<Btr90Entity> type, Level level) {
        super(type, level);
        getPositionTransform().put("MissileRack", this::getMissileRackTransform);
        getVectorTransform().put("MissileRack", this::getMissileRackVector);
        getRotationTransform().put("MissileRack", partialTicks ->
                getMissileRackTransform(partialTicks).getNormalizedRotation(new Quaterniond()));
    }

    public float getMissileRackElevation(float partialTicks) {
        return Btr90ModelTransforms.missileElevation(getTurretXRotO(), getTurretXRot(), partialTicks);
    }

    public Matrix4d getMissileRackTransform(float partialTicks) {
        return Btr90ModelTransforms.missileRackTransform(
                getTurretTransform(partialTicks), getMissileRackElevation(partialTicks));
    }

    public Vec3 getMissileRackVector(float partialTicks) {
        Vector3d direction = getMissileRackTransform(partialTicks)
                .transformDirection(new Vector3d(0, 0, 1)).normalize();
        return new Vec3(direction.x, direction.y, direction.z);
    }

    // The rear grenade barrel is mechanically linked to the main gun.
    // Retain its own pivot while sharing the turret's yaw and elevation.
    @Override
    public float getGunYRot() { return getTurretYRot(); }

    @Override
    public float getGunYRotO() { return getTurretYRotO(); }

    @Override
    public float getGunXRot() { return getTurretXRot(); }

    @Override
    public float getGunXRotO() { return getTurretXRotO(); }
}
