package com.rhythm.dragon_vehicle_deployer;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue DEFAULT_SPAWN_INTERVAL;
    public static final ModConfigSpec.IntValue IDLE_CLEAR_TIMEOUT_SECONDS;

    static {
        BUILDER.push("General");

        DEFAULT_SPAWN_INTERVAL = BUILDER
                .comment("Default auto-spawn interval in seconds for new deployers")
                .defineInRange("defaultSpawnInterval", 5, 5, 3600);

        IDLE_CLEAR_TIMEOUT_SECONDS = BUILDER
                .comment("Seconds of idle time after which a vehicle that has been occupied will be cleared and respawned. Set to 0 to disable.")
                .defineInRange("idleClearTimeoutSeconds", 300, 0, 36000);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
