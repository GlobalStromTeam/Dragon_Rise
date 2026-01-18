package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.CM34Model;
import com.redabysslucia.dragonrise_reforge.entities.CM34Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CM34Renderer extends VehicleRenderer<CM34Entity> {
        public CM34Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new CM34Model());
        }
}