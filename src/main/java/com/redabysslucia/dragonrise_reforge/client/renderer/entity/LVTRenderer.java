package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.LVTModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M2A2Model;
import com.redabysslucia.dragonrise_reforge.entities.LVTEntity;
import com.redabysslucia.dragonrise_reforge.entities.M2A2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class LVTRenderer extends VehicleRenderer<LVTEntity> {
        public LVTRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new LVTModel());
        }
}
