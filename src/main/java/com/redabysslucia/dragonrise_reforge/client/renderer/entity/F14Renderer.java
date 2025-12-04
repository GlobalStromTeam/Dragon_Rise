package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.F14Model;
import com.redabysslucia.dragonrise_reforge.entities.F14Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class F14Renderer extends VehicleRenderer<F14Entity> {
        public F14Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new F14Model());
        }
}
