package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.M113Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M113Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M113Renderer extends VehicleRenderer<M113Entity> {
        public M113Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new M113Model());
        }
}
