package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Leopard2a4Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Leopard2a4Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Leopard2a4Renderer extends VehicleRenderer<Leopard2a4Entity> {
        public Leopard2a4Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Leopard2a4Model());
        }
}
