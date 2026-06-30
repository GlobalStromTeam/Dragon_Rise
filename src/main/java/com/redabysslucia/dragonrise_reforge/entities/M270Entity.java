package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

@SuppressWarnings("removal")
public class M270Entity extends GeoVehicleEntity {

    public M270Entity(EntityType<M270Entity> type, Level world) {
        super(type, world);
    }

    private boolean isAngleRestricted() {
        float turretYaw = getTurretYRot();
        float turretPitch = getTurretXRot();
        return Mth.abs(turretYaw) <= 50f && -turretPitch < 30f;
    }

    @Override
    public boolean canShoot(LivingEntity living) {
        if (isAngleRestricted()) {
            return false;
        }
        return super.canShoot(living);
    }

    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        if (isAngleRestricted()) {
            if (living instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.dragonrise_reforge.m270_angle_restricted"), true);
            }
            return;
        }
        super.vehicleShoot(living, uuid, targetPos);
    }
}
