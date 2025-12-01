package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZBL08Model;
import com.redabysslucia.dragonrise_reforge.entities.ZBL08Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZBL08Renderer extends VehicleRenderer<ZBL08Entity> {
        public ZBL08Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZBL08Model());
        }
}