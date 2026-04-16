package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J10CGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.F16CModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10CModel;
import com.redabysslucia.dragonrise_reforge.entities.F16CEntity;
import com.redabysslucia.dragonrise_reforge.entities.J10CEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class F16CRenderer extends VehicleRenderer<F16CEntity> {
        public F16CRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new F16CModel());
            //this.addRenderLayer(new J10CGlowLayer(this));
        }
}
