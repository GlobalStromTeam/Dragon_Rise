package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.projectile.JavelinMissileEntity;
import com.atsuishio.superbwarfare.entity.projectile.Ru9m336MissileEntity;
import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import com.redabysslucia.dragonrise_reforge.config.GroundMissileConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class GroundMissileGuidance {
    public record BeforeTick(Vec3 motion, Vec3 look) {}
    public static final String TAG = "DragonriseGroundMissile";
    public static final int WIRE = 1, ANTI_AIR = 2, ANTI_TANK = 3;
    private static final Set<String> LAUNCHERS = Set.of(
            "625e", "9m133", "akm", "bmd4m", "bmp3", "bmpt72", "btr90", "cv90", "flarakpz1",
            "hj8", "humveetow", "l1a2", "m1128", "m3a3", "tunguska", "vt4a1", "vt4b", "zbd04a",
            "zbd05", "zbl08", "zlt11", "zsu234", "ztd05", "ztq15", "ztz99a", "ztz99ah", "ztz99bh");

    private GroundMissileGuidance() {}

    public static boolean eligibleLauncher(Entity entity) {
        if (!(entity instanceof VehicleEntity)) return false;
        ResourceLocation id = EntityType.getKey(entity.getType());
        return id.getNamespace().equals("dragonrise_reforge") && LAUNCHERS.contains(id.getPath());
    }

    public static boolean delayedMissile(VehicleEntity vehicle, GunData gun) {
        return eligibleLauncher(vehicle) && gun != null && gun.get(GunProp.SHOOT_DELAY_TIME) > 0
                && gun.get(GunProp.PROJECTILE).getItemId().contains("missile");
    }

    public static boolean supported(Entity entity) {
        return entity instanceof WireGuideMissileEntity || entity instanceof Ru9m336MissileEntity
                || entity instanceof JavelinMissileEntity;
    }

    public static void mark(Entity projectile, Entity launcher) {
        if (!eligibleLauncher(launcher) || !supported(projectile)) return;
        int profile = projectile instanceof WireGuideMissileEntity ? WIRE
                : projectile instanceof Ru9m336MissileEntity ? ANTI_AIR : ANTI_TANK;
        CompoundTag tag = new CompoundTag();
        tag.putInt("Profile", profile);
        tag.putString("Launcher", EntityType.getKey(launcher.getType()).toString());
        projectile.getPersistentData().put(TAG, tag);
    }

    public static int profile(Entity projectile) {
        return projectile.getPersistentData().getCompound(TAG).getInt("Profile");
    }

    public static BeforeTick capture(Entity projectile) {
        return profile(projectile) == 0 ? null : new BeforeTick(projectile.getDeltaMovement(), projectile.getLookAngle());
    }

    public static boolean spawn(ServerLevel level, Entity projectile, Entity launcher) {
        mark(projectile, launcher);
        boolean added = level.addFreshEntity(projectile);
        if (added && eligibleLauncher(launcher) && supported(projectile)) MissileLaunchGate.launched((VehicleEntity) launcher);
        return added;
    }

    public static void writeSpawn(Entity entity, FriendlyByteBuf buffer) {
        if (supported(entity)) buffer.writeNbt(profile(entity) == 0 ? null : entity.getPersistentData().getCompound(TAG));
    }

    public static void readSpawn(Entity entity, FriendlyByteBuf buffer) {
        if (!supported(entity)) return;
        CompoundTag tag = buffer.readNbt();
        if (tag != null && tag.getInt("Profile") >= WIRE && tag.getInt("Profile") <= ANTI_TANK) {
            entity.getPersistentData().put(TAG, tag);
        } else entity.getPersistentData().remove(TAG);
    }

    public static void writeBvr(Entity entity, CompoundTag tag) {
        if (profile(entity) == 0) return;
        CompoundTag forge = tag.getCompound("ForgeData");
        forge.put(TAG, entity.getPersistentData().getCompound(TAG).copy());
        tag.put("ForgeData", forge);
    }

    public static double turnLimit(Entity projectile) {
        return switch (profile(projectile)) {
            case WIRE -> Math.min(GroundMissileConfig.WIRE.get(), Math.max(0, (projectile.tickCount - 1) * 0.4));
            case ANTI_AIR -> Math.min(GroundMissileConfig.ANTI_AIR.get(), Math.max(0, (projectile.tickCount - 1) * 0.5));
            case ANTI_TANK -> isTerminalTopAttack(projectile)
                    ? GroundMissileConfig.TOP_TERMINAL.get() : GroundMissileConfig.ANTI_TANK.get();
            default -> 180.0;
        };
    }

    private static boolean isTerminalTopAttack(Entity entity) {
        if (!(entity instanceof JavelinMissileEntity missile) || !missile.isTop()) return false;
        Entity target = EntityFindUtil.findEntity(entity.level(), missile.getTargetUUID());
        Vec3 pos = target == null ? missile.getTargetPos() : target.position();
        return pos != null && pos.subtract(entity.position()).horizontalDistanceSqr() < 900.0;
    }

    public static void finishTick(Entity projectile, Vec3 beforeMotion, Vec3 beforeLook) {
        if (beforeMotion == null || projectile.isRemoved()) return;
        double limit = turnLimit(projectile);
        Vec3 motion = projectile.getDeltaMovement(), look = projectile.getLookAngle();
        Vec3 reference = beforeMotion.lengthSqr() < 1.0e-12 ? beforeLook : beforeMotion;
        Vec3 limitedMotion = MissileTurnMath.limit(reference, motion, limit);
        Vec3 limitedLook = MissileTurnMath.limit(beforeLook, look, limit);
        if (limitedMotion != motion) projectile.setDeltaMovement(limitedMotion);
        // Keep native steering inertia, but cap both the nose and the actual velocity in 3-D.
        if (limitedLook != look) {
            float yaw = (float) Math.toDegrees(Math.atan2(-limitedLook.x, limitedLook.z));
            float pitch = (float) Math.toDegrees(Math.atan2(-limitedLook.y, limitedLook.horizontalDistance()));
            projectile.setYRot(projectile.getYRot() + Mth.wrapDegrees(yaw - projectile.getYRot()));
            projectile.setXRot(pitch);
        }
    }
}
