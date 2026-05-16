package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TunguskaModel;
import com.redabysslucia.dragonrise_reforge.entities.TunguskaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TunguskaRenderer extends VehicleRenderer<TunguskaEntity> {
        public TunguskaRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new TunguskaModel());
        }
}
