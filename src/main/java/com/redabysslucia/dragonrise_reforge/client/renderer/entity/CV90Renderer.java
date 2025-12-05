package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.CV90Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZBD04AModel;
import com.redabysslucia.dragonrise_reforge.entities.CV90Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CV90Renderer extends VehicleRenderer<CV90Entity> {
        public CV90Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new CV90Model());
        }
}