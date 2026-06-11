package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.M270Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M270Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M270Renderer extends VehicleRenderer<M270Entity> {
        public M270Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new M270Model());
        }
}
