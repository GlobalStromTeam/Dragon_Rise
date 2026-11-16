package com.redabysslucia.dragonrise_reforge.entities.utils;

import net.minecraft.world.phys.Vec3;

/** Caps the combined 3-D angle, retaining the candidate vector's magnitude. */
public final class MissileTurnMath {
    private static final double EPSILON = 1.0e-12;

    private MissileTurnMath() {}

    public static Vec3 limit(Vec3 previous, Vec3 candidate, double degrees) {
        double beforeLength = previous.length(), speed = candidate.length();
        if (beforeLength < EPSILON || speed < EPSILON || !Double.isFinite(beforeLength + speed)) return candidate;
        Vec3 from = previous.scale(1.0 / beforeLength), to = candidate.scale(1.0 / speed);
        double dot = Math.max(-1.0, Math.min(1.0, from.dot(to)));
        double angle = Math.acos(dot), cap = Math.toRadians(Math.max(0.0, Math.min(180.0, degrees)));
        if (angle <= cap + 1.0e-12) return candidate;
        Vec3 tangent = to.subtract(from.scale(dot));
        if (tangent.lengthSqr() < EPSILON) {
            // Opposite vectors have no unique turn plane; choose one deterministically on both sides.
            Vec3 axis = Math.abs(from.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            tangent = axis.subtract(from.scale(from.dot(axis)));
        }
        Vec3 direction = from.scale(Math.cos(cap)).add(tangent.normalize().scale(Math.sin(cap)));
        return direction.normalize().scale(speed);
    }
}
