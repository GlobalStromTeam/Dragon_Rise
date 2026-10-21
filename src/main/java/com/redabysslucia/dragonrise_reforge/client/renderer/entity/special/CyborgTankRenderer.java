package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.redabysslucia.dragonrise_reforge.client.renderer.entity.AnimatedTrackedVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.special.CyborgTankEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CyborgTankRenderer extends AnimatedTrackedVehicleRenderer<CyborgTankEntity> {
        public CyborgTankRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "cyborg_tank");
        }
}
