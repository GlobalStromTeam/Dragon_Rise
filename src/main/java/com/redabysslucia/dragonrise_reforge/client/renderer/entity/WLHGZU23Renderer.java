package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.WLHGZU23Model;
import com.redabysslucia.dragonrise_reforge.entities.WLHGZU23Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class WLHGZU23Renderer extends VehicleRenderer<WLHGZU23Entity> {
        public WLHGZU23Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new WLHGZU23Model());
        }
}