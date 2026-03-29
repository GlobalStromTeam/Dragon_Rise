package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.NH90Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z20Model;
import com.redabysslucia.dragonrise_reforge.entities.NH90Entity;
import com.redabysslucia.dragonrise_reforge.entities.Z20Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NH90Renderer extends VehicleRenderer<NH90Entity> {
        public NH90Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new NH90Model());
        }
}
