package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AH64Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.EC665Model;
import com.redabysslucia.dragonrise_reforge.entities.AH64Entity;
import com.redabysslucia.dragonrise_reforge.entities.EC665Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AH64Renderer extends VehicleRenderer<AH64Entity> {
        public AH64Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new AH64Model());
        }
}
