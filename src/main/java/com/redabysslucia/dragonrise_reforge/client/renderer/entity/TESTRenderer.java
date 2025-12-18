package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TESTModel;
import com.redabysslucia.dragonrise_reforge.entities.TESTEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TESTRenderer extends VehicleRenderer<TESTEntity> {
        public TESTRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new TESTModel());
        }
}
