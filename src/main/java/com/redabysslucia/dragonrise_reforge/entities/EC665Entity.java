package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class EC665Entity extends NightVisionVehicle {

        public EC665Entity(EntityType<EC665Entity> type, Level world) {
                super(type, world);
        }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }
}
