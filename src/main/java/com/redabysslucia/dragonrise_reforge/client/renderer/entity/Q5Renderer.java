package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.Q5GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.REFALEGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.F14Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Q5Model;
import com.redabysslucia.dragonrise_reforge.entities.F14Entity;
import com.redabysslucia.dragonrise_reforge.entities.Q5Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Q5Renderer extends VehicleRenderer<Q5Entity> {
        public Q5Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new Q5Model());
                 this.addRenderLayer(new Q5GlowLayer(this));
        }
}
