package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.client.layer.J20GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20Model;
import com.redabysslucia.dragonrise_reforge.entities.J20Entity;
import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J20Renderer extends VehicleRenderer<J20Entity> {
        public J20Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new J20Model());
                this.addRenderLayer(new J20GlowLayer(this));
        }
}
