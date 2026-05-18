package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Dragonrise_reforge.MODID);

    public static final RegistryObject<SoundEvent> TERRORIST_IDLE = REGISTRY.register("terrorist_idle", () -> 
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "terrorist_idle")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_STEP = REGISTRY.register("terrorist_step", () -> 
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "terrorist_step")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_HURT = REGISTRY.register("terrorist_hurt", () -> 
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "terrorist_hurt")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_DEATH = REGISTRY.register("terrorist_death", () -> 
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "terrorist_death")));
    
    public static final RegistryObject<SoundEvent> TERRORIST_SHOOT = REGISTRY.register("terrorist_shoot", () -> 
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "terrorist_shoot")));

    public static final RegistryObject<SoundEvent> SUPPLY_STATION_CHARGING = REGISTRY.register("supply_station_charging", () ->
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "supply_station_charging")));

    public static final RegistryObject<SoundEvent> SUPPLY_STATION_COMPLETE = REGISTRY.register("supply_station_complete", () ->
        SoundEvent.createVariableRangeEvent(new ResourceLocation(Dragonrise_reforge.MODID, "supply_station_complete")));
}