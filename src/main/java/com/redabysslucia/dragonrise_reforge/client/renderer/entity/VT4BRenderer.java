package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.VT4A1Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.VT4BModel;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4BEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class VT4BRenderer extends VehicleRenderer<VT4BEntity> {
        public VT4BRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new VT4BModel());
        }
}
