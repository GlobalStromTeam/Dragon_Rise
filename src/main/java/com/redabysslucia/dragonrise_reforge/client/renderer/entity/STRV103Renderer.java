package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.STRV103Model;
import com.redabysslucia.dragonrise_reforge.entities.STRV103Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class STRV103Renderer extends VehicleRenderer<STRV103Entity> {
        public STRV103Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new STRV103Model());
        }
}