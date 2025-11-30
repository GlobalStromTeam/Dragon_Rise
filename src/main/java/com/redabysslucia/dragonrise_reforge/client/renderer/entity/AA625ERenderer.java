package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AA625EModel;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AA625ERenderer extends VehicleRenderer<AA625EEntity> {
        public AA625ERenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new AA625EModel());
        }
}
