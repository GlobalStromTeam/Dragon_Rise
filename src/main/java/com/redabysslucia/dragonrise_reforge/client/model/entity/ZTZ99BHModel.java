package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.M1A2SEPV2Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99BHEntity;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class ZTZ99BHModel extends VehicleModel<ZTZ99BHEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return false;
        }

//        @Override
//        public ResourceLocation getAnimationResource(ZTZ99BHEntity entity) {
//                return new ResourceLocation(Dragonrise_reforge.MODID,"animations/ztz99bh.animation.json");
//        }
//
//        @Override
//        public ResourceLocation getModelResource(ZTZ99BHEntity entity) {
//                return new ResourceLocation(Dragonrise_reforge.MODID, "geo/ztz99bh.geo.json");
//        }
//
//        @Override
//        public ResourceLocation getTextureResource(ZTZ99BHEntity entity) {
//                return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/ztz99bh.png");
//        }
}
