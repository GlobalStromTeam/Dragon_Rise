package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.EC665Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z10MEModel;
import com.redabysslucia.dragonrise_reforge.entities.EC665Entity;
import com.redabysslucia.dragonrise_reforge.entities.Z10MEEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EC665Renderer extends VehicleRenderer<EC665Entity> {
        public EC665Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new EC665Model());
        }
}
