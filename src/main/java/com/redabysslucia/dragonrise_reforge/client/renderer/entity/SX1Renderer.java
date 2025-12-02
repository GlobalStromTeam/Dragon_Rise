package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.SX1Model;
import com.redabysslucia.dragonrise_reforge.entities.SX1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SX1Renderer extends VehicleRenderer<SX1Entity> {
        public SX1Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new SX1Model());
        }
}