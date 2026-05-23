package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M3StuartModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.type97Model;
import com.redabysslucia.dragonrise_reforge.entities.M3StuartEntity;
import com.redabysslucia.dragonrise_reforge.entities.type97Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class type97Renderer extends VehicleRenderer<type97Entity> {
        public type97Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new type97Model());
        }
}
