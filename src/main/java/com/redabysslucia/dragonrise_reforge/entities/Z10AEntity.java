package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class Z10AEntity extends NightVisionVehicle {

        public Z10AEntity(EntityType<Z10AEntity> type, Level world) {
                super(type, world);
        }

    @Override
    public ResourceLocation getNightVisionShader() {
        return new ResourceLocation("shaders/post/night-vision-wp.json");
    }
}
