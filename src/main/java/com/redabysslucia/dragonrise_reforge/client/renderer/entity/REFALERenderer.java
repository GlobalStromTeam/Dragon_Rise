package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.REFALEModel;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import com.redabysslucia.dragonrise_reforge.entities.REFALEEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class REFALERenderer extends VehicleRenderer<REFALEEntity> {
        public REFALERenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new REFALEModel());
        }
}
