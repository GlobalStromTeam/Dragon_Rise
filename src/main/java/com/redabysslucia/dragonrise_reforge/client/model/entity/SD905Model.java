package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.SD905Entity;
import com.redabysslucia.dragonrise_reforge.entities.utils.TestShipEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SD905Model extends DragonriseVehicleModel<SD905Entity> {

    private static final float SPEED_FACTOR = 0.5f;

    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }

    @Override
    public @Nullable TransformContext<SD905Entity> collectTransform(String boneName) {
        if (boneName.equals("propeller")) {
            return (bone, vehicle, state) -> {
                float rotation = calculatePropellerRotation(vehicle, state.getPartialTick());
                bone.setRotY(rotation);
            };
        }

        if (boneName.equals("propeller2")) {
            return (bone, vehicle, state) -> {
                float rotation = calculatePropellerRotation(vehicle, state.getPartialTick());
                bone.setRotY(rotation);
            };
        }

//        if (boneName.equals("tailPropeller")) {
//            return (bone, vehicle, state) -> bone.setRotX(-6 * Mth.lerp(state.getPartialTick(), vehicle.getPropellerRotO(), vehicle.getPropellerRot()));
//        }

        if (boneName.equals("bomb1")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 1));
        }

        if (boneName.equals("bomb2")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 2));
        }

        if (boneName.equals("bomb3")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 3));
        }

        if (boneName.equals("bomb4")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 4));
        }

        if (boneName.equals("bomb5")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 5));
        }

        if (boneName.equals("bomb6")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 6));
        }

        if (boneName.equals("bomb7")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 7));
        }

        if (boneName.equals("bomb8")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 8));
        }

        if (boneName.equals("bomb9")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 7));
        }

        if (boneName.equals("bomb10")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 8));
        }

        if (boneName.equals("bomb11")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 7));
        }

        if (boneName.equals("bomb12")) {
            return (bone, vehicle, state) -> bone.setHidden(shouldHideMissile(vehicle, 8));
        }

        return super.collectTransform(boneName);
    }

    public boolean shouldHideMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("bomb");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }

    private float calculatePropellerRotation(SD905Entity vehicle, float partialTick) {

        double speed = getEntitySpeed(vehicle);

        float maxRotationSpeed = 360.0f;
        float maxSpeed = 20.0f;
        float clampedSpeed = (float) Math.min(speed, maxSpeed);
        float speedRatio = clampedSpeed / maxSpeed;
        float currentRotationSpeed = maxRotationSpeed * speedRatio * SPEED_FACTOR;
        float rotationAngle = (System.currentTimeMillis() % 3600) * currentRotationSpeed / 10.0f;
        return (float) Math.toRadians(rotationAngle);
    }

    private double getEntitySpeed(Entity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        double speedMetersPerTick = velocity.length();
        return speedMetersPerTick * 20.0;
    }
}