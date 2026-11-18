// Adapted from Frontline Combat Pack (Commissar_Mado), commit e4188472.
// Original license: META-INF/licenses/frontline-combat-pack.txt.
package com.redabysslucia.dragonrise_reforge.entity.vehicle.engine;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine start/stop for Dragonrise ground vehicles and helicopters.
 *
 * Starting is a hold: while the engine is off, holding the engine key cranks it (STARTING, the
 * start sound plays) and it catches once the vehicle's start time has passed (RUNNING). Letting
 * go early aborts back to OFF. Stopping is a single press while running and is instant.
 *
 * The client reports the held key every tick ({@link #holdStart}); the server aborts a start that
 * stops getting those. The state is a synced byte on VehicleEngineControlMixin (see
 * {@link EngineControlled}) so the HUD bar and the engine sound player can read it client-side.
 *
 * Applies only to the vehicles in {@link #START_TICKS} with a Wheel, Track or Helicopter engine.
 * Helicopters only hold for the ignition (HELICOPTER_HOLD_TICKS); the rotor spool-up then runs on.
 */
public final class EngineControl {

    public static final byte OFF = 0;
    public static final byte STARTING = 1;
    public static final byte RUNNING = 2;

    /**
     * Vehicles with engine start/stop, and how long each takes to start: the length of its
     * Squad start sound as the driver hears it (interior), in ticks. Generated from the matched recordings in the engine audio library.
     */
    public static final Map<String, Integer> START_TICKS = startTicks();

    private static Map<String, Integer> startTicks() {
        Map<String, Integer> m = new java.util.HashMap<>();
        put(m, 16, "dragonrise_reforge:motuo");
        put(m, 23, "dragonrise_reforge:bkan1c", "dragonrise_reforge:cv90");
        put(m, 25, "dragonrise_reforge:fav_a", "dragonrise_reforge:sx1");
        put(m, 29, "dragonrise_reforge:cav", "dragonrise_reforge:csk181");
        put(m, 31, "dragonrise_reforge:cm34");
        put(m, 35, "dragonrise_reforge:zsd89a", "dragonrise_reforge:zsd89a_covered");
        put(m, 40, "dragonrise_reforge:amx56", "dragonrise_reforge:challenger_ds", "dragonrise_reforge:k1a1", "dragonrise_reforge:l1a2", "dragonrise_reforge:leopard2a4", "dragonrise_reforge:merkava_mk4");
        put(m, 41, "dragonrise_reforge:m1126", "dragonrise_reforge:m1128", "dragonrise_reforge:m1296");
        put(m, 42, "dragonrise_reforge:m10booker", "dragonrise_reforge:m113");
        put(m, 43, "dragonrise_reforge:aav7a1", "dragonrise_reforge:aavc7c1", "dragonrise_reforge:flarakpz1", "dragonrise_reforge:m270", "dragonrise_reforge:m3a3", "dragonrise_reforge:zbd05", "dragonrise_reforge:zsd05", "dragonrise_reforge:ztd05");
        put(m, 47, "dragonrise_reforge:ural4320", "dragonrise_reforge:ural4320_supply", "dragonrise_reforge:ural4320_zu23");
        put(m, 49, "dragonrise_reforge:bmpt72", "dragonrise_reforge:btr90", "dragonrise_reforge:chonmaho5", "dragonrise_reforge:t72b3", "dragonrise_reforge:t90mh");
        put(m, 55, "dragonrise_reforge:625e", "dragonrise_reforge:zbl08", "dragonrise_reforge:zlt11", "dragonrise_reforge:zsl10");
        put(m, 56, "dragonrise_reforge:zbd04a", "dragonrise_reforge:ztq15");
        put(m, 64, "dragonrise_reforge:type100", "dragonrise_reforge:vt4a1", "dragonrise_reforge:vt4b", "dragonrise_reforge:ztz96a", "dragonrise_reforge:ztz99a", "dragonrise_reforge:ztz99ah", "dragonrise_reforge:ztz99bh");
        put(m, 66, "dragonrise_reforge:tunguska", "dragonrise_reforge:zsu234");
        put(m, 70, "dragonrise_reforge:humvee", "dragonrise_reforge:humveetow");
        put(m, 71, "dragonrise_reforge:ztz59a");
        put(m, 72, "dragonrise_reforge:akm", "dragonrise_reforge:brdm2");
        put(m, 80, "dragonrise_reforge:mv3", "dragonrise_reforge:mv3_armed", "dragonrise_reforge:mv3_supply");
        put(m, 85, "dragonrise_reforge:2s25m", "dragonrise_reforge:2s38", "dragonrise_reforge:bmd4m", "dragonrise_reforge:bmp3");
        put(m, 110, "dragonrise_reforge:toyota_seiki", "dragonrise_reforge:wlhgzu23", "dragonrise_reforge:wlsc");
        put(m, 260, "dragonrise_reforge:m1a1hc", "dragonrise_reforge:m1a2sepv1", "dragonrise_reforge:m1a2sepv2", "dragonrise_reforge:project640", "dragonrise_reforge:t80", "dragonrise_reforge:t80b");
        put(m, 400, "dragonrise_reforge:ah1f", "dragonrise_reforge:ah64", "dragonrise_reforge:ka50", "dragonrise_reforge:mi24p", "dragonrise_reforge:mi24v", "dragonrise_reforge:nh90", "dragonrise_reforge:uh60", "dragonrise_reforge:z20");
        put(m, 415, "dragonrise_reforge:ec665", "dragonrise_reforge:z10a", "dragonrise_reforge:z10me", "dragonrise_reforge:z9");
        return Map.copyOf(m);
    }

    private static void put(Map<String, Integer> m, int ticks, String... ids) {
        for (String id : ids) m.put(id, ticks);
    }

    /** A start aborts if the hold stops being reported for longer than this. */
    private static final int HOLD_TIMEOUT = 3;

    /**
     * Helicopters: the rotor spool-up (~20 s) is far too long to hold a key through, so only this
     * much is a hold (the ignition); after it the start runs on by itself.
     */
    public static final int HELICOPTER_HOLD_TICKS = 20;

    /**
     * Vehicles whose engine is voiced by FCP's Squad engine player instead of SBW's EngineSound.
     * Filled client-side by the profile loader; stays empty on a dedicated server.
     */
    public static final Set<String> SQUAD_SOUND_VEHICLES = ConcurrentHashMap.newKeySet();

    private static final Map<VehicleEntity, Integer> STARTED_AT = new WeakHashMap<>();
    private static final Map<VehicleEntity, Integer> LAST_HOLD = new WeakHashMap<>();

    private EngineControl() {
    }

    public static String id(VehicleEntity vehicle) {
        return EntityType.getKey(vehicle.getType()).toString();
    }

    public static boolean appliesTo(VehicleEntity vehicle) {
        if (!(vehicle instanceof EngineControlled) || !START_TICKS.containsKey(id(vehicle))) return false;
        EngineType type = vehicle.computed().getEngineType();
        return type == EngineType.WHEEL || type == EngineType.TRACK || type == EngineType.HELICOPTER;
    }

    public static boolean isHelicopter(VehicleEntity vehicle) {
        return vehicle.computed().getEngineType() == EngineType.HELICOPTER;
    }

    /** Ticks the key must be held for a start to commit: the whole start, or the ignition on helicopters. */
    public static int holdTicks(VehicleEntity vehicle) {
        return isHelicopter(vehicle) ? Math.min(HELICOPTER_HOLD_TICKS, startTicks(vehicle)) : startTicks(vehicle);
    }

    /** No fuel/energy left: the engine can't start, and a running one dies. */
    public static boolean outOfFuel(VehicleEntity vehicle) {
        return vehicle.hasEnergyStorage() && vehicle.getEnergy() <= 0;
    }

    public static int startTicks(VehicleEntity vehicle) {
        return START_TICKS.getOrDefault(id(vehicle), 40);
    }

    public static byte state(VehicleEntity vehicle) {
        return vehicle instanceof EngineControlled c ? c.dragonrise$getEngineState() : RUNNING;
    }

    /** True when drive inputs must be ignored. */
    public static boolean blocksInput(VehicleEntity vehicle) {
        return appliesTo(vehicle) && state(vehicle) != RUNNING;
    }

    /** True when SBW's own engine loop should be silent because a Squad profile replaces it. */
    public static boolean usesSquadSound(VehicleEntity vehicle) {
        return !SQUAD_SOUND_VEHICLES.isEmpty() && SQUAD_SOUND_VEHICLES.contains(id(vehicle));
    }

    /** Server: the driver is holding the engine key. Begins a start from OFF, keeps one going. */
    public static void holdStart(VehicleEntity vehicle) {
        if (!appliesTo(vehicle) || vehicle.isWreck()) return;
        EngineControlled c = (EngineControlled) vehicle;
        byte state = c.dragonrise$getEngineState();
        if (state == OFF) {
            if (outOfFuel(vehicle)) return;
            c.dragonrise$setEngineState(STARTING);
            STARTED_AT.put(vehicle, vehicle.tickCount);
        }
        if (state != RUNNING) LAST_HOLD.put(vehicle, vehicle.tickCount);
    }

    /** Server: the driver pressed the engine key while it runs. Instant shutdown. */
    public static void stop(VehicleEntity vehicle) {
        if (!appliesTo(vehicle)) return;
        setOff(vehicle);
    }

    /** Helicopter idle: turns the rotor without lifting. (SBW spools to 0.12, which lifts some helicopters.) */
    public static final float HELICOPTER_IDLE_POWER = 0.05f;
    /**
     * Engine-off wind-down: the rotor loses whatever power it had at shutdown over ~21.5 s, roughly
     * the length of Squad's rotor stop sounds (16-28 s). The time is shared by every helicopter and
     * does not depend on the power, so a rotor at idle (landed) keeps turning as long as one shut
     * down at full power - a fixed per-tick loss had idle rotors stop in about a second and a half.
     */
    private static final int HELICOPTER_SPIN_DOWN_TICKS = 430;
    /** Per-tick power loss for the current wind-down, fixed from the power at shutdown. */
    private static final Map<VehicleEntity, Float> SPIN_DOWN = new WeakHashMap<>();

    /**
     * Server: SBW turns the rotor from its power value, so power follows our engine state - it
     * spools up to idle over the start, never drops below idle on the ground while running (the
     * rotor keeps turning after landing), and winds down to nothing once the engine is off instead
     * of staying wherever the throttle was left.
     */
    private static void rotorPower(VehicleEntity vehicle, byte state) {
        float power = vehicle.getPower();
        if (state == STARTING) {
            Integer started = STARTED_AT.get(vehicle);
            float progress = started == null ? 0f : Math.min(1f, (vehicle.tickCount - started) / (float) startTicks(vehicle));
            vehicle.setPower(HELICOPTER_IDLE_POWER * progress);
        } else if (state == RUNNING) {
            if (vehicle.onGround() && power < HELICOPTER_IDLE_POWER) vehicle.setPower(HELICOPTER_IDLE_POWER);
        } else if (power != 0f) {
            float step = SPIN_DOWN.computeIfAbsent(vehicle, v -> Math.abs(power) / HELICOPTER_SPIN_DOWN_TICKS);
            vehicle.setPower(power > 0 ? Math.max(0f, power - step) : Math.min(0f, power + step));
            return;
        }
        SPIN_DOWN.remove(vehicle);
    }

    private static void setOff(VehicleEntity vehicle) {
        ((EngineControlled) vehicle).dragonrise$setEngineState(OFF);
        STARTED_AT.remove(vehicle);
        LAST_HOLD.remove(vehicle);
    }

    /**
     * Tick (both sides): keeps SBW's helicopter start flags in step with ours; server-side also
     * finishes or aborts a start and kills the engine on a wreck or an empty tank.
     */
    public static void tick(VehicleEntity vehicle) {
        if (!(vehicle instanceof EngineControlled c) || !appliesTo(vehicle)) return;
        byte state = c.dragonrise$getEngineState();
        if (isHelicopter(vehicle)) {
            // SBW spools a helicopter up when the pilot presses up with its engine off; ours replaces
            // that start, so SBW's flags simply mirror our state (inputs are blocked until RUNNING).
            boolean running = state == RUNNING;
            if (vehicle.getEngineStart() != running) vehicle.setEngineStart(running);
            if (vehicle.getEngineStartOver() != running) vehicle.setEngineStartOver(running);
        }
        if (vehicle.level().isClientSide()) return;
        if (isHelicopter(vehicle)) rotorPower(vehicle, state);
        if (state == OFF) return;
        if (vehicle.isWreck() || (state == RUNNING && outOfFuel(vehicle))) {
            setOff(vehicle);
            return;
        }
        if (state == STARTING) {
            Integer started = STARTED_AT.get(vehicle);
            Integer held = LAST_HOLD.get(vehicle);
            // No record (loaded mid-start) or the key was let go: the start fails.
            boolean released = held == null || vehicle.tickCount - held > HOLD_TIMEOUT;
            boolean committed = started != null && vehicle.tickCount - started >= holdTicks(vehicle);
            if (started == null || (released && !committed)) {
                setOff(vehicle);
            } else if (vehicle.tickCount - started >= startTicks(vehicle)) {
                c.dragonrise$setEngineState(RUNNING);
                STARTED_AT.remove(vehicle);
                LAST_HOLD.remove(vehicle);
            }
        }
    }
}
