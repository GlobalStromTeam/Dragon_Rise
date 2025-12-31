package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z10MEModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z9Model;
import com.redabysslucia.dragonrise_reforge.entities.Z10MEEntity;
import com.redabysslucia.dragonrise_reforge.entities.Z9Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Z9Renderer extends VehicleRenderer<Z9Entity> {
        public Z9Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new Z9Model());
        }
}
