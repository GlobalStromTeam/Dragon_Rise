package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J10GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J20GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20Model;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J10Renderer extends VehicleRenderer<J10Entity> {
        public J10Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new J10Model());
            this.addRenderLayer(new J10GlowLayer(this));
        }
}
