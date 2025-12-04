package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZLT11Model;
import com.redabysslucia.dragonrise_reforge.entities.ZLT11Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZLT11Renderer extends VehicleRenderer<ZLT11Entity> {
        public ZLT11Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZLT11Model());
        }
}