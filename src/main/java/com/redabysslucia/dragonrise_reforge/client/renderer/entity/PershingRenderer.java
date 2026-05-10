package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.PershingModel;
import com.redabysslucia.dragonrise_reforge.entities.PershingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PershingRenderer extends VehicleRenderer<PershingEntity> {
    public PershingRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PershingModel());
    }
}
