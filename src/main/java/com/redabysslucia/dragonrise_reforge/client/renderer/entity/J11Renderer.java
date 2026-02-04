package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J11GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10CModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J11Model;
import com.redabysslucia.dragonrise_reforge.entities.J10CEntity;
import com.redabysslucia.dragonrise_reforge.entities.J11Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J11Renderer extends VehicleRenderer<J11Entity> {
        public J11Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new J11Model());
            this.addRenderLayer(new J11GlowLayer(this));
        }
}
