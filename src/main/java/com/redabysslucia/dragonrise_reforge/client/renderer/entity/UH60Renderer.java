package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.UH60Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z9Model;
import com.redabysslucia.dragonrise_reforge.entities.UH60Entity;
import com.redabysslucia.dragonrise_reforge.entities.Z9Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class UH60Renderer extends VehicleRenderer<UH60Entity> {
        public UH60Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new UH60Model());
        }
}
