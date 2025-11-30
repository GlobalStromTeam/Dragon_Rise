package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class AA625EModel extends VehicleModel<AA625EEntity> {

        @Override
        public boolean hideForTurretControllerWhileZooming() {
                return true;
        }

        @Override
        public ResourceLocation getModelResource(AA625EEntity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "geo/625e.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(AA625EEntity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/625e.png");
        }
}
