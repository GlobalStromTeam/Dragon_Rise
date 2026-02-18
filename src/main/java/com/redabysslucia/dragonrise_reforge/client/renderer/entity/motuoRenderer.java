package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.WLSCGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.WLSCModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.motuoModel;
import com.redabysslucia.dragonrise_reforge.entities.WLSCEntity;
import com.redabysslucia.dragonrise_reforge.entities.motuoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class motuoRenderer extends VehicleRenderer<motuoEntity> {
        public motuoRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new motuoModel());
        }
}