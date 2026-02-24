package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ59AModel;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ59AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZTZ59ARenderer extends VehicleRenderer<ZTZ59AEntity> {
        public ZTZ59ARenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZTZ59AModel());
        }
}
