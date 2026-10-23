package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.M3StuartEntity;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M3StuartRenderer extends AnimatedTrackedVehicleRenderer<M3StuartEntity> {
        public M3StuartRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "m3stuart");
        }
}
