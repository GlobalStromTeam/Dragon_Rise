package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J10GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J8GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J8Model;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import com.redabysslucia.dragonrise_reforge.entities.J8Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J8Renderer extends VehicleRenderer<J8Entity> {
        public J8Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new J8Model());
            this.addRenderLayer(new J8GlowLayer(this));
        }
}
