package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZBL11Model;
import com.redabysslucia.dragonrise_reforge.entities.ZBL11Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZBL11Renderer extends VehicleRenderer<ZBL11Entity> {
        public ZBL11Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZBL11Model());
        }
}