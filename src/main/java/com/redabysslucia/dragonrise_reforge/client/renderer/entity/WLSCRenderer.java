package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J10GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.WLSCGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TOYOTASEIKIModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.WLSCModel;
import com.redabysslucia.dragonrise_reforge.entities.TOYOTASEIKIEntity;
import com.redabysslucia.dragonrise_reforge.entities.WLSCEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class WLSCRenderer extends VehicleRenderer<WLSCEntity> {
        public WLSCRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new WLSCModel());
                this.addRenderLayer(new WLSCGlowLayer(this));
        }
}