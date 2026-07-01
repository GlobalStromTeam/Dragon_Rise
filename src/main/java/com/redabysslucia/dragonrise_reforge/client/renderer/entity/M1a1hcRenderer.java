package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.M1a1hcEntity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M1a1hcModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M1a1hcRenderer extends VehicleRenderer<M1a1hcEntity> {
        public M1a1hcRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new M1a1hcModel());
        }
}
