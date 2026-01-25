package com.redabysslucia.dragonrise_reforge.client.renderer.entity.hmg;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.hmg.DSHKModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.hmg.M2Model;
import com.redabysslucia.dragonrise_reforge.entities.hmg.DSHKEntity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.M2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DSHKRenderer extends VehicleRenderer<DSHKEntity> {
    public DSHKRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DSHKModel());
    }
}
