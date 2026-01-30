package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.CV90Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M3A3Model;
import com.redabysslucia.dragonrise_reforge.entities.CV90Entity;
import com.redabysslucia.dragonrise_reforge.entities.M3A3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M3A3Renderer extends VehicleRenderer<M3A3Entity> {
        public M3A3Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new M3A3Model());
        }
}