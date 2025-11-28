package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class ZTZ99AModel extends VehicleModel<ZTZ99AEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

        @Override
        public ResourceLocation getAnimationResource(ZTZ99AEntity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID,"animations/ztz99a.animation.json");
        }

        @Override
        public ResourceLocation getModelResource(ZTZ99AEntity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "geo/ztz99a.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(ZTZ99AEntity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/ztz99a.png");
        }
}
