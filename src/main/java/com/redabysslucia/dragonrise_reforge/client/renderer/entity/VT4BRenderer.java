package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4BEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class VT4BRenderer extends AnimatedTrackedVehicleRenderer<VT4BEntity> {
        public VT4BRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "vt4b");
        }
}
