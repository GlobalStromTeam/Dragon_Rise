package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.J11Entity;
import com.redabysslucia.dragonrise_reforge.entities.syy651Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class syy651Renderer extends GeoVehicleRenderer<syy651Entity> {
        public syy651Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager);
            //this.addRenderLayer(new J11GlowLayer(this));
        }
}
