package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.TJGCGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.TJGCLaserLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.F16CModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TJGCModel;
import com.redabysslucia.dragonrise_reforge.entities.F16CEntity;
import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TJGCRenderer extends VehicleRenderer<TJGCEntity> {
        public TJGCRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new TJGCModel());
            this.addRenderLayer(new TJGCGlowLayer(this));
            this.addRenderLayer(new TJGCLaserLayer(this));
        }
}
