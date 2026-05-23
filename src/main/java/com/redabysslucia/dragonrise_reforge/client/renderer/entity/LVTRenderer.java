package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.LVTModel;
import com.redabysslucia.dragonrise_reforge.entities.LVTEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class LVTRenderer extends VehicleRenderer<LVTEntity> {
        public LVTRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new LVTModel());
        }
}
