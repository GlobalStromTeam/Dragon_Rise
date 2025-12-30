package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.CamelModel;
import com.redabysslucia.dragonrise_reforge.entities.CamelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CamelRenderer extends VehicleRenderer<CamelEntity> {
        public CamelRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new CamelModel());

        }
}
