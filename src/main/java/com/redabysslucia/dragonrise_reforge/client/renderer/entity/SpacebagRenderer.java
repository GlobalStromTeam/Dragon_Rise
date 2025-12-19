package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.SpacebagModel;
import com.redabysslucia.dragonrise_reforge.entities.SpacebagEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SpacebagRenderer extends VehicleRenderer<SpacebagEntity> {
        public SpacebagRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new SpacebagModel());
        }
}
