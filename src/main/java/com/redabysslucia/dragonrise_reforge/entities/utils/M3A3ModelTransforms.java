package com.redabysslucia.dragonrise_reforge.entities.utils;

import org.joml.Matrix4d;

/** Shared rack pose for rendering, projectile origins and damage OBBs. */
public final class M3A3ModelTransforms {
    public static final int RACK_ANIM_TICKS = 30;

    private M3A3ModelTransforms() { }

    public static float rackRollDegrees(int state, int timer) {
        float progress = switch (state) {
            case 1 -> 1f - timer / (float) RACK_ANIM_TICKS;
            case 2 -> 1f;
            case 3 -> timer / (float) RACK_ANIM_TICKS;
            default -> 0f;
        };
        return -90f * Math.max(0f, Math.min(1f, progress));
    }

    public static Matrix4d towTransform(Matrix4d turretTransform, float modelRollDegrees) {
        // (tow.pivot - turret.pivot) / 16, with Bedrock -Z mapped to vehicle +Z.
        return new Matrix4d(turretTransform)
                .translate(17.19233 / 16.0, 11.52986 / 16.0, 5.72127 / 16.0)
                .rotateZ(-Math.toRadians(modelRollDegrees));
    }
}
