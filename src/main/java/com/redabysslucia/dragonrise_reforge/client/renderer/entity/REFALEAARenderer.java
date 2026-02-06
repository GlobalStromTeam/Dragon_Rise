package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.REFALEAAGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.REFALEGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.REFALEAAModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.REFALEModel;
import com.redabysslucia.dragonrise_reforge.entities.REFALEAAEntity;
import com.redabysslucia.dragonrise_reforge.entities.REFALEEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class REFALEAARenderer extends VehicleRenderer<REFALEAAEntity> {
        public REFALEAARenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new REFALEAAModel());
                this.addRenderLayer(new REFALEAAGlowLayer(this));
        }
}
