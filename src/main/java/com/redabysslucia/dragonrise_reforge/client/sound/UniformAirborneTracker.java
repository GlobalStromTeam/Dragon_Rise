package com.redabysslucia.dragonrise_reforge.client.sound;

/** Observes one takeoff/landing pair without treating spawning or equipping in midair as a jump. */
public final class UniformAirborneTracker {
    public enum Action { NONE, JUMP, LAND }

    private boolean positioned;
    private boolean grounded;
    private boolean armed;
    private boolean airborne;
    private int takeoffTicks;
    private double launchY;
    private double lastX;
    private double lastY;
    private double lastZ;

    public Action update(double x, double y, double z, boolean onGround, boolean eligible) {
        boolean wasGrounded = grounded;
        double previousY = lastY;
        double horizontal = Math.hypot(x - lastX, z - lastZ);
        double vertical = y - lastY;
        lastX = x;
        lastY = y;
        lastZ = z;
        grounded = onGround;
        if (!positioned) {
            positioned = true;
            armed = eligible && onGround;
            return Action.NONE;
        }
        if (!eligible || !Double.isFinite(horizontal) || !Double.isFinite(vertical)
                || horizontal > 1.5 || Math.abs(vertical) > 8) {
            armed = eligible && onGround;
            airborne = false;
            takeoffTicks = 0;
            return Action.NONE;
        }
        if (onGround) {
            boolean landed = airborne;
            armed = true;
            airborne = false;
            takeoffTicks = 0;
            return landed ? Action.LAND : Action.NONE;
        }
        if (wasGrounded && armed) {
            airborne = true;
            launchY = previousY;
            // Remote ground flags can arrive before their interpolated position starts rising.
            takeoffTicks = 3;
        }
        if (airborne && takeoffTicks > 0) {
            takeoffTicks--;
            if (y > launchY + 0.01) {
                takeoffTicks = 0;
                return Action.JUMP;
            }
        }
        return Action.NONE;
    }
}
