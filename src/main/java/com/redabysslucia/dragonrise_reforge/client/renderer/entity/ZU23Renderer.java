package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZU23Model;
import com.redabysslucia.dragonrise_reforge.entities.ZU23Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZU23Renderer extends VehicleRenderer<ZU23Entity> {
    public ZU23Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ZU23Model());
    }
}
