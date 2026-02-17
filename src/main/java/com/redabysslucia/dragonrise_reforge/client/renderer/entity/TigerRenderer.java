package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TigerModel;
import com.redabysslucia.dragonrise_reforge.entities.TigerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TigerRenderer extends VehicleRenderer<TigerEntity> {
        public TigerRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new TigerModel());
        }
}