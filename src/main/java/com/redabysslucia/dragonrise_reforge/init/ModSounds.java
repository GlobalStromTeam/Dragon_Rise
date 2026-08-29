package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Dragonrise_reforge.MODID);

    public static final RegistryObject<SoundEvent> TERRORIST_IDLE = REGISTRY.register("terrorist_idle", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_idle")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_STEP = REGISTRY.register("terrorist_step", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_step")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_HURT = REGISTRY.register("terrorist_hurt", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_hurt")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_DEATH = REGISTRY.register("terrorist_death", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_death")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_SHOOT = REGISTRY.register("terrorist_shoot", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_shoot")));

    public static final RegistryObject<SoundEvent> SUPPLY_STATION_CHARGING = REGISTRY.register("supply_station_charging", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "supply_station_charging")));

    public static final RegistryObject<SoundEvent> SUPPLY_STATION_COMPLETE = REGISTRY.register("supply_station_complete", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "supply_station_complete")));

    // 侦察无人车音效
    public static final RegistryObject<SoundEvent> R6_DRONE_MOVING = REGISTRY.register("r6_drone_moving", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "r6_drone_moving")));
    public static final RegistryObject<SoundEvent> R6_DRONE_FAST = REGISTRY.register("r6_drone_fast", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "r6_drone_fast")));
    public static final RegistryObject<SoundEvent> R6_DRONE_JUMP = REGISTRY.register("r6_drone_jump", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "r6_drone_jump")));
    public static final RegistryObject<SoundEvent> R6_DRONE_DOWN = REGISTRY.register("r6_drone_down", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "r6_drone_down")));

    /** 载具音效（开火/装填/引擎等）。仅写 sounds.json 不会被注册进 ForgeRegistries.SOUND_EVENTS，
     *  1.20.1 里 Level.playSound 需要 Holder<SoundEvent>，未注册的事件会静默失败。 */
    private static final String[] VEHICLE_SOUNDS = {
        "cyborg_tank_engine",
        "cyborg_tank_fire_1p",
        "cyborg_tank_fire_3p",
        "cyborg_tank_fire_3p_far",
        "cyborg_tank_fire_3p_very_far",
        "cyborg_tank_fire_coax",
        "cyborg_tank_fire_coax_far",
        "cyborg_tank_fire_station",
        "cyborg_tank_fire_station_far",
        "cyborg_tank_reload",
        "fire_sound_57mm",
        "fire_sound_57mm_far",
        "hand_reload3s",
        "hand_reload5s",
        "jet_engine",
        "m1a2_engine",
        "m1a2_fire_1p",
        "m1a2_fire_3p",
        "m1a2_fire_3p_far",
        "m1a2_reload",
        "m2a3_engine_loop",
        "m2a3fire",
        "m2a3firefar",
        "mgfire1",
        "mgfire11",
        "plane_loop",
        "t80_engine",
        "t80_fire_1p",
        "t80_fire_3p",
        "t80_fire_3p_far",
        "t80_reload",
        "tank_engine_loop3",
        "type100_engine",
        "us_105_shoot",
        "us_37_shoot",
        "us_76_shoot",
        "wheel_loop1",
        "zbd04a_100mm_fire_1p",
        "zbd04a_100mm_fire_3p",
        "zbd04a_100mm_fire_3p_far",
        "zbd04a_100mm_reload",
        "zbd04a_30mm_fire_1p",
        "zbd04a_30mm_fire_3p",
        "zbd04a_30mm_fire_3p_far",
        "ztz59_fire_1p",
        "ztz59_fire_3p_far",
        "ztz99a_engine",
        "ztz99a_fire_1p",
        "ztz99a_fire_3p",
        "ztz99a_fire_3p_far",
        "ztz99a_reload"
    };

    static {
        for (String soundName : VEHICLE_SOUNDS) {
            REGISTRY.register(soundName, () ->
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, soundName)));
        }
    }
}
