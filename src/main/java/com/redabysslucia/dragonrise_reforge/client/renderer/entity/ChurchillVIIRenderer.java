package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ChurchillVIIModel;
import com.redabysslucia.dragonrise_reforge.entities.ChurchillVIIEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ChurchillVIIRenderer extends VehicleRenderer<ChurchillVIIEntity> {
    public ChurchillVIIRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChurchillVIIModel());
    }
}
