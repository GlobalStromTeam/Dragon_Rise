package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.type3Model;
import com.redabysslucia.dragonrise_reforge.entities.type3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class type3Renderer extends VehicleRenderer<type3Entity> {
        public type3Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new type3Model());
        }
}
