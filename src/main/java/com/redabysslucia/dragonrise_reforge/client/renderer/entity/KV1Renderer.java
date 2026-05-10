package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.KV1Model;
import com.redabysslucia.dragonrise_reforge.entities.KV1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class KV1Renderer extends VehicleRenderer<KV1Entity> {
        public KV1Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new KV1Model());
        }
}
