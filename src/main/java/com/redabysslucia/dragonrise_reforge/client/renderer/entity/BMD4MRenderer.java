package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.BMD4MModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.BMP3Model;
import com.redabysslucia.dragonrise_reforge.entities.BMD4MEntity;
import com.redabysslucia.dragonrise_reforge.entities.BMP3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BMD4MRenderer extends VehicleRenderer<BMD4MEntity> {
        public BMD4MRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new BMD4MModel());
        }
}