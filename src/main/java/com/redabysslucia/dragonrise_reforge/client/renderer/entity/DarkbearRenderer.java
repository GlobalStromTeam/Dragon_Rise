package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.DarkbearEntity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.DarkbearModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DarkbearRenderer extends VehicleRenderer<DarkbearEntity> {
        public DarkbearRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new DarkbearModel());
        }
}
