package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.BMPT72Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZBD04AModel;
import com.redabysslucia.dragonrise_reforge.entities.BMPT72Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BMPT72Renderer extends VehicleRenderer<BMPT72Entity> {
        public BMPT72Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new BMPT72Model());
        }
}