package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class T80Model extends VehicleModel<T80Entity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

        @Override
        public ResourceLocation getModelResource(T80Entity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "geo/t80.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T80Entity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/t80.png");
        }
}
