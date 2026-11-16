package com.redabysslucia.dragonrise_reforge.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server config is synchronized by Forge; units are degrees per game tick, not physical G. */
public final class GroundMissileConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue WIRE, ANTI_AIR, ANTI_TANK, TOP_TERMINAL;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Turn limits for Dragonrise ground-launched missiles, in degrees/tick.").push("turn_limits");
        WIRE = builder.defineInRange("wire_guided", 2.0, 0.0, 180.0);
        ANTI_AIR = builder.defineInRange("anti_air", 4.0, 0.0, 180.0);
        ANTI_TANK = builder.defineInRange("anti_tank", 3.0, 0.0, 180.0);
        TOP_TERMINAL = builder.defineInRange("top_attack_terminal", 4.0, 0.0, 180.0);
        builder.pop();
        SPEC = builder.build();
    }

    private GroundMissileConfig() {}
}
