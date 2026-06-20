package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, Dragonrise_reforge.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TERRORIST_IDLE = REGISTRY.register("terrorist_idle", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_idle")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRORIST_STEP = REGISTRY.register("terrorist_step", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_step")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRORIST_HURT = REGISTRY.register("terrorist_hurt", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_hurt")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRORIST_DEATH = REGISTRY.register("terrorist_death", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_death")));
    
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRORIST_SHOOT = REGISTRY.register("terrorist_shoot", () -> 
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "terrorist_shoot")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SUPPLY_STATION_CHARGING = REGISTRY.register("supply_station_charging", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "supply_station_charging")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SUPPLY_STATION_COMPLETE = REGISTRY.register("supply_station_complete", () ->
        SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "supply_station_complete")));
}