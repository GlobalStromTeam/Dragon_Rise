package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.FAVAModel;
import com.redabysslucia.dragonrise_reforge.entities.FAVAEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FAVARenderer extends VehicleRenderer<FAVAEntity> {
        public FAVARenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new FAVAModel());
        }
}