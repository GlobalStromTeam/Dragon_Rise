package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TYPE100Model;
import com.redabysslucia.dragonrise_reforge.entities.TYPE100Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TYPE100Renderer extends VehicleRenderer<TYPE100Entity> {
        public TYPE100Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new TYPE100Model());
        }
}
