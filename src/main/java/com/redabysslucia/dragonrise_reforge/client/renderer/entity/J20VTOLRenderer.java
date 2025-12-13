package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J20VTOLGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20VTOLModel;
import com.redabysslucia.dragonrise_reforge.entities.J20VTOLEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J20VTOLRenderer extends VehicleRenderer<J20VTOLEntity> {
        public J20VTOLRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new J20VTOLModel());
                this.addRenderLayer(new J20VTOLGlowLayer(this));
        }
}
