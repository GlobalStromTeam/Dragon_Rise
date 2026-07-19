package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Zsl10Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Zsl10Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Zsl10Renderer extends VehicleRenderer<Zsl10Entity> {
        public Zsl10Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Zsl10Model());
        }
}
