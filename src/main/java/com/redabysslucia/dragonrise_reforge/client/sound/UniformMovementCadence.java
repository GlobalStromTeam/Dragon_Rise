package com.redabysslucia.dragonrise_reforge.client.sound;

/** Distance-driven movement rhythm; short network position gaps do not mean a remote player stopped. */
public final class UniformMovementCadence {
    public enum Movement { IDLE, WALK, CROUCH, SPRINT }

    private static final double MIN_MOVEMENT = 0.002;
    private static final double TELEPORT_DISTANCE = 1.5;
    private static final int STOP_GRACE_TICKS = 4;
    private static final double CROUCH_FREQUENCY = 0.75;
    private static final float VANILLA_DISTANCE_FACTOR = 0.6F;
    private boolean positioned;
    private boolean movedThisTick;
    private double lastX;
    private double lastZ;
    private double travelled;
    private float stepDistance;
    private float nextStep = 1;
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
        ticksSinceSound += CROUCH_FREQUENCY;
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
            boolean regularGaitChange = movement != Movement.CROUCH && requested != Movement.CROUCH;
            movement = requested;
            travelled = 0;
            ticksSinceSound = 0;
            if (!regularGaitChange) {
                stepDistance = 0;
                nextStep = 1;
                return true;
            }
        }
        if (requested != Movement.CROUCH) {
            // Entity.move()/nextStep(): flat-ground distance * 0.6, then the next integer.
            // Keep the same phase when switching between walking and sprinting.
            stepDistance += (float)distance * VANILLA_DISTANCE_FACTOR;
            if (stepDistance > nextStep) {
                nextStep = (int)stepDistance + 1;
                return true;
            }
            return false;
        }
        travelled += distance * CROUCH_FREQUENCY;
        double stride = 0.9;
        double interval = Math.max(9, Math.ceil(fragmentTicks));
        if (travelled >= stride && ticksSinceSound >= interval) {
            travelled %= stride;
            // Retain sub-tick timing without banking idle time for a later burst.
            ticksSinceSound = Math.min(ticksSinceSound - interval, CROUCH_FREQUENCY);
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
        stepDistance = 0;
        nextStep = 1;
        quietTicks = 0;
        ticksSinceSound = 0;
    }
}
