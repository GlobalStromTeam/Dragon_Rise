package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Ah1fEntity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Ah1fModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Ah1fRenderer extends VehicleRenderer<Ah1fEntity> {
        public Ah1fRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Ah1fModel());
        }
}
