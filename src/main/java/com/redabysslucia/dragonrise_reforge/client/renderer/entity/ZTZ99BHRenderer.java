package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99BHModel;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99BHEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZTZ99BHRenderer extends VehicleRenderer<ZTZ99BHEntity> {
        public ZTZ99BHRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZTZ99BHModel());
        }
}
