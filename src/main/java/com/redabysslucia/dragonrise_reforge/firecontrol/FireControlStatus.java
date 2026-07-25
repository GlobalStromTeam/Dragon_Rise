package com.redabysslucia.dragonrise_reforge.firecontrol;

import java.util.Locale;

public enum FireControlStatus {
    INACTIVE,
    MOVING,
    ALIGNING,
    READY,
    OUT_OF_RANGE,
    PITCH_LIMIT,
    YAW_LIMIT,
    INVALID_INPUT,
    INVALID_WEAPON,
    WRECKED;

    public static FireControlStatus fromId(int id) {
        FireControlStatus[] values = values();
        return id >= 0 && id < values.length ? values[id] : INACTIVE;
    }

    public String translationKey() {
        return "status.dragonrise_reforge.fire_control." + name().toLowerCase(Locale.ROOT);
    }
}
