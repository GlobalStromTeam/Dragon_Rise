package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.MarkvEntity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.MarkvModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MarkvRenderer extends VehicleRenderer<MarkvEntity> {
        public MarkvRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new MarkvModel());
        }
}
