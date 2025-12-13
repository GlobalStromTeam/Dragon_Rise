package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.SHIELDModel;
import com.redabysslucia.dragonrise_reforge.entities.SHIELDEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SHIELDRenderer extends VehicleRenderer<SHIELDEntity> {
    public SHIELDRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SHIELDModel());
    }
}
