package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J20GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J35GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J35Model;
import com.redabysslucia.dragonrise_reforge.entities.J20Entity;
import com.redabysslucia.dragonrise_reforge.entities.J35Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J35Renderer extends VehicleRenderer<J35Entity> {
        public J35Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new J35Model());
                this.addRenderLayer(new J35GlowLayer(this));
        }
}
