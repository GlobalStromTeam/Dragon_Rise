package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.type97Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.type97QModel;
import com.redabysslucia.dragonrise_reforge.entities.type97Entity;
import com.redabysslucia.dragonrise_reforge.entities.type97QEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class type97QRenderer extends VehicleRenderer<type97QEntity> {
        public type97QRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new type97QModel());
        }
}
