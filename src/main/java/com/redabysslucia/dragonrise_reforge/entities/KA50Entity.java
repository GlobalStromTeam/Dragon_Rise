package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class KA50Entity extends NightVisionVehicle {

        public KA50Entity(EntityType<KA50Entity> type, Level world) {
                super(type, world);
        }

        @Override
        public ResourceLocation getNightVisionShader() {
                return new ResourceLocation("shaders/post/night-vision-bw.json");
        }


//        @Override
//        public double getMouseSensitivity() {
//                return 0.25;
//        }
}
