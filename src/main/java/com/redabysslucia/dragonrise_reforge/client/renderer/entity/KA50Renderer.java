package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.KA50Model;
import com.redabysslucia.dragonrise_reforge.entities.KA50Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class KA50Renderer extends VehicleRenderer<KA50Entity> {
        public KA50Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new KA50Model());
        }
}
