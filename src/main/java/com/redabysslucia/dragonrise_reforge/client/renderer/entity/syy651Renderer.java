package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J11GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J11Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.syy651Model;
import com.redabysslucia.dragonrise_reforge.entities.J11Entity;
import com.redabysslucia.dragonrise_reforge.entities.syy651Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class syy651Renderer extends VehicleRenderer<syy651Entity> {
        public syy651Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new syy651Model());
            //this.addRenderLayer(new J11GlowLayer(this));
        }
}
