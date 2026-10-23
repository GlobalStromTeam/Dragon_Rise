package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.MausEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MausRenderer extends AnimatedTrackedVehicleRenderer<MausEntity> {
    public MausRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, "maus");
    }
}
