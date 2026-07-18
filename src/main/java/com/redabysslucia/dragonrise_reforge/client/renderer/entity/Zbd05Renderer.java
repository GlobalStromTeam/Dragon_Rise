package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Zbd05Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Zbd05Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Zbd05Renderer extends VehicleRenderer<Zbd05Entity> {
        public Zbd05Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Zbd05Model());
        }
}
