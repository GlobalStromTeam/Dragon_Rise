package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import net.minecraft.resources.ResourceLocation;

public class J10Model extends VehicleModel<J10Entity> {

        @Override
        public ResourceLocation getModelResource(J10Entity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "geo/j10.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(J10Entity entity) {
                return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/j10.png");
        }
}