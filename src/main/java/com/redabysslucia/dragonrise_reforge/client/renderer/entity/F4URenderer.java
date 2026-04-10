package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.CamelModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.F4UModel;
import com.redabysslucia.dragonrise_reforge.entities.CamelEntity;
import com.redabysslucia.dragonrise_reforge.entities.F4UEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class F4URenderer extends VehicleRenderer<F4UEntity> {
        public F4URenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new F4UModel());

        }
}
