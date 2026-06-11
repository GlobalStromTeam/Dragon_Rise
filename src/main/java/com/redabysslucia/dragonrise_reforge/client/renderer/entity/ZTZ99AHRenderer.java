package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AHModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AModel;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AHEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZTZ99AHRenderer extends VehicleRenderer<ZTZ99AHEntity> {
        public ZTZ99AHRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZTZ99AHModel());
        }
}
