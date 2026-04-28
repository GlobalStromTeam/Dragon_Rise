package com.redabysslucia.dragonrise_reforge.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.redabysslucia.dragonrise_reforge.Dragonrise_reforge.MODID;

public class ModSpeedSounds {
    
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> PLANE_HIGH_SPEED = SOUNDS.register("plane_high_speed", 
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "plane_high_speed")));
    
    public static final RegistryObject<SoundEvent> PLANE_SUPERSONIC_LOOP = SOUNDS.register("plane_supersonic_loop", 
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "plane_supersonic_loop")));

    public static final RegistryObject<SoundEvent> PLANE_DISTANT_SOUND = SOUNDS.register("plane_distant_sound", 
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "plane_distant_sound")));
}
