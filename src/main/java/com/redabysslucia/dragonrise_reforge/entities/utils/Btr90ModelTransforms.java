package com.redabysslucia.dragonrise_reforge.entities.utils;

import org.joml.Matrix4d;

/** Launcher pose shared by rendering, projectile origins and damage OBBs. */
public final class Btr90ModelTransforms {
    public static final float MISSILE_MIN_ELEVATION = -5f;
    public static final float MISSILE_MAX_ELEVATION = 30f;

    private Btr90ModelTransforms() { }

    public static float missileElevation(float previousPitch, float pitch, float partialTicks) {
        // SBW stores positive downward pitch; limits describe upward elevation.
        float elevation = -(previousPitch + (pitch - previousPitch) * partialTicks);
        return Math.max(MISSILE_MIN_ELEVATION, Math.min(MISSILE_MAX_ELEVATION, elevation));
    }

    public static Matrix4d missileRackTransform(Matrix4d turretTransform, float elevation) {
        // Scaled model support hinge relative to TurretPos, in vehicle coordinates.
        return new Matrix4d(turretTransform)
                .translate(0.0, 0.2275646768, -0.1818748180)
                .rotateX(-Math.toRadians(elevation));
    }
}
