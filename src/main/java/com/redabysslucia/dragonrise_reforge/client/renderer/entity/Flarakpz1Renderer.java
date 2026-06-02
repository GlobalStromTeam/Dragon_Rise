package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Flarakpz1Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Flarakpz1Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Flarakpz1Renderer extends VehicleRenderer<Flarakpz1Entity> {
        public Flarakpz1Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Flarakpz1Model());
        }
}
