package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.MausModel;
import com.redabysslucia.dragonrise_reforge.entities.MausEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MausRenderer extends VehicleRenderer<MausEntity> {
    public MausRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MausModel());
    }
}
