package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.M1A2SEPV2Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.resources.ResourceLocation;

public class M1A2SEPV2Model extends DragonriseVehicleModel<M1A2SEPV2Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

//        @Override
//        public ResourceLocation getAnimationResource(M1A2SEPV2Entity entity) {
//                return new ResourceLocation(Dragonrise_reforge.MODID,"animations/m1a2sepv2.animation.json");
//        }
//
//        @Override
//        public ResourceLocation getModelResource(M1A2SEPV2Entity entity) {
//                return new ResourceLocation(Dragonrise_reforge.MODID, "geo/m1a2sepv2.geo.json");
//        }
//
//        @Override
//        public ResourceLocation getTextureResource(M1A2SEPV2Entity entity) {
//                return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/m1a2sepv2.png");
//        }
}
