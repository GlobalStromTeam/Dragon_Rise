package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.vehicle.SlidingDoorHelicopterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

@SuppressWarnings("removal")
public class UH60Entity extends SlidingDoorHelicopterEntity {

        public UH60Entity(EntityType<UH60Entity> type, Level world) {
                super(type, world, "uh60");
        }


}
