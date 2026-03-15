package com.rhythm.dragon_vehicle_deployer;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue DEFAULT_SPAWN_INTERVAL;

    static {
        BUILDER.push("General");

        DEFAULT_SPAWN_INTERVAL = BUILDER
                .comment("Default auto-spawn interval in seconds for new deployers")
                .defineInRange("defaultSpawnInterval", 5, 5, 3600);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
