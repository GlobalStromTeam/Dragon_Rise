package com.redabysslucia.dragonrise_reforge.client.animation;

import com.google.gson.annotations.SerializedName;

public enum AnimationPlayType {
    @SerializedName("play_once_stop")
    PLAY_ONCE_STOP,

    @SerializedName("play_once_hold")
    PLAY_ONCE_HOLD,

    @SerializedName("loop")
    LOOP;

    public static AnimationPlayType fromString(String str) {
        if (str == null) return LOOP;
        return switch (str.toLowerCase()) {
            case "play_once_stop" -> PLAY_ONCE_STOP;
            case "play_once_hold" -> PLAY_ONCE_HOLD;
            default -> LOOP;
        };
    }
}