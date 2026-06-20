package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AHEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99BHEntity;

@SuppressWarnings("removal")
public class ZTZ99AHModel extends DragonriseVehicleModel<ZTZ99AHEntity> {

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
//                return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "geo/ztz99bh.geo.json");
//        }
//
//        @Override
//        public ResourceLocation getTextureResource(ZTZ99BHEntity entity) {
//                return ResourceLocation.fromNamespaceAndPath(Dragonrise_reforge.MODID, "textures/entity/ztz99bh.png");
//        }
}
