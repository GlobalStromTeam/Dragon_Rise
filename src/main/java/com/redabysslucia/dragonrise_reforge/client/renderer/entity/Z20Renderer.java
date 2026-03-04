package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.UH60Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z20Model;
import com.redabysslucia.dragonrise_reforge.entities.UH60Entity;
import com.redabysslucia.dragonrise_reforge.entities.Z20Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Z20Renderer extends VehicleRenderer<Z20Entity> {
        public Z20Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new Z20Model());
        }
}
