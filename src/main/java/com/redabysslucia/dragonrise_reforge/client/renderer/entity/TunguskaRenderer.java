package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.TunguskaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TunguskaRenderer extends AnimatedTrackedVehicleRenderer<TunguskaEntity> {
        public TunguskaRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "tunguska");
        }
}
