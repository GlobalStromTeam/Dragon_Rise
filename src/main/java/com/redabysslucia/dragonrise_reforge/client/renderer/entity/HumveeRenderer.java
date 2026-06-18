package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.HumveeModel;
import com.redabysslucia.dragonrise_reforge.entities.HumveeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HumveeRenderer extends VehicleRenderer<HumveeEntity> {
        public HumveeRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new HumveeModel());
        }
}