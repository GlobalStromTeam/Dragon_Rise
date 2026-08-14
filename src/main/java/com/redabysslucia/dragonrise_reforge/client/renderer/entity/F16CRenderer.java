package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.F16CEntity;
import com.redabysslucia.dragonrise_reforge.entities.J10CEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class F16CRenderer extends GeoVehicleRenderer<F16CEntity> {
        public F16CRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager);
            //this.addRenderLayer(new J10CGlowLayer(this));
        }
}
