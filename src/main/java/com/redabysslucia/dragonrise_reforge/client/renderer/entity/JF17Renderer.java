package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10CModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.JF17Model;
import com.redabysslucia.dragonrise_reforge.entities.J10CEntity;
import com.redabysslucia.dragonrise_reforge.entities.JF17Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class JF17Renderer extends VehicleRenderer<JF17Entity> {
        public JF17Renderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new JF17Model());
            //this.addRenderLayer(new J10GlowLayer(this));
        }
}
