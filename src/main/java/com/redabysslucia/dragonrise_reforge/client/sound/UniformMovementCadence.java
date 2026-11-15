package com.redabysslucia.dragonrise_reforge.client.sound;

/** Distance-driven movement rhythm; short network position gaps do not mean a remote player stopped. */
public final class UniformMovementCadence {
    public enum Movement { IDLE, WALK, CROUCH, SPRINT }

    private static final double MIN_MOVEMENT = 0.002;
    private static final double TELEPORT_DISTANCE = 1.5;
    private static final int STOP_GRACE_TICKS = 4;
    private static final double PLAYBACK_FREQUENCY = 1.5;
    private boolean positioned;
    private boolean movedThisTick;
    private double lastX;
    private double lastZ;
    private double travelled;
    private int quietTicks;
    private double ticksSinceSound;
    private double fragmentTicks = 14;
    private Movement movement = Movement.IDLE;

    /** Called once per client tick. Returns true only when a new movement fragment is due. */
    public boolean update(double x, double z, Movement requested) {
        movedThisTick = false;
        if (!positioned) {
            lastX = x;
            lastZ = z;
            positioned = true;
            return false;
        }
        double distance = Math.hypot(x - lastX, z - lastZ);
        lastX = x;
        lastZ = z;
        if (requested == Movement.IDLE || distance > TELEPORT_DISTANCE || !Double.isFinite(distance)) {
            reset();
            return false;
        }
        double frequency = PLAYBACK_FREQUENCY * (requested == Movement.CROUCH ? 0.5 : 1.0);
        ticksSinceSound += frequency;
        if (distance <= MIN_MOVEMENT) {
            if (++quietTicks >= STOP_GRACE_TICKS) reset();
            return false;
        }
        quietTicks = 0;
        movedThisTick = true;
        if (movement == Movement.IDLE) {
            movement = requested;
            travelled = 0;
            ticksSinceSound = 0;
            return true;
        }
        if (movement != requested) {
            movement = requested;
            travelled = 0;
            ticksSinceSound = 0;
            return true;
        }
        travelled += distance * frequency;
        double stride = requested == Movement.CROUCH ? 0.9 : requested == Movement.SPRINT ? 1.65 : 1.5;
        int minimumTicks = requested == Movement.CROUCH ? 9 : requested == Movement.SPRINT ? 5 : 6;
        // Previously fragments could not overlap, so their length also limited the cadence.
        // Speed up that reference clock together with the distance rhythm, not the audio itself.
        double interval = Math.max(minimumTicks, Math.ceil(fragmentTicks));
        if (travelled >= stride && ticksSinceSound >= interval) {
            travelled %= stride;
            // Retain sub-tick timing without banking idle time for a later burst.
            ticksSinceSound = Math.min(ticksSinceSound - interval, frequency);
            return true;
        }
        return false;
    }

    public Movement movement() {
        return movement;
    }

    public boolean movedThisTick() {
        return movedThisTick;
    }

    public void fragmentDurationTicks(double ticks) {
        fragmentTicks = ticks;
    }

    private void reset() {
        movedThisTick = false;
        movement = Movement.IDLE;
        travelled = 0;
        quietTicks = 0;
        ticksSinceSound = 0;
    }
}
