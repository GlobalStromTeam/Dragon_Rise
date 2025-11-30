package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AModel;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZTZ99ARenderer extends VehicleRenderer<ZTZ99AEntity> {
        public ZTZ99ARenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZTZ99AModel());
        }
}
