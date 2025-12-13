package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.F14GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J16GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.F14Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J16Model;
import com.redabysslucia.dragonrise_reforge.entities.F14Entity;
import com.redabysslucia.dragonrise_reforge.entities.J16Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J16Renderer extends VehicleRenderer<J16Entity> {
        public J16Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new J16Model());
            this.addRenderLayer(new J16GlowLayer(this));

        }
}
