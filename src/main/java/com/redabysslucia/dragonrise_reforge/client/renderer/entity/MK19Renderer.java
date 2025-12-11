package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.MK19Entity;
import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.MK19Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MK19Renderer extends VehicleRenderer<MK19Entity> {
    public MK19Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MK19Model());
    }
}
