package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.NPDS810Model;
import com.redabysslucia.dragonrise_reforge.entities.NPDS810Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NPDS810Renderer extends VehicleRenderer<NPDS810Entity> {
        public NPDS810Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new NPDS810Model());
        }
}
