package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J10CGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J10GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10CModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10Model;
import com.redabysslucia.dragonrise_reforge.entities.J10CEntity;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J10CRenderer extends VehicleRenderer<J10CEntity> {
        public J10CRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new J10CModel());
            this.addRenderLayer(new J10CGlowLayer(this));
        }
}
