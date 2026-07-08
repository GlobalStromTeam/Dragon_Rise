package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.HumveetowEntity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.HumveetowModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HumveetowRenderer extends VehicleRenderer<HumveetowEntity> {
        public HumveetowRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new HumveetowModel());
        }
}
