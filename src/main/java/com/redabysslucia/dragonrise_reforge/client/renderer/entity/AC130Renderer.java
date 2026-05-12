package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AC130Model;
import com.redabysslucia.dragonrise_reforge.entities.AC130Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AC130Renderer extends VehicleRenderer<AC130Entity> {
    public AC130Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AC130Model());
    }
}
