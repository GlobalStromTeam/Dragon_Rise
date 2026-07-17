package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Ztd05Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Ztd05Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Ztd05Renderer extends VehicleRenderer<Ztd05Entity> {
        public Ztd05Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Ztd05Model());
        }
}
