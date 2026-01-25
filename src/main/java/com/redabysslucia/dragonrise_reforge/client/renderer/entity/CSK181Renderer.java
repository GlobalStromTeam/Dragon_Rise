package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.CSK181Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TOYOTASEIKIModel;
import com.redabysslucia.dragonrise_reforge.entities.CSK181Entity;
import com.redabysslucia.dragonrise_reforge.entities.TOYOTASEIKIEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CSK181Renderer extends VehicleRenderer<CSK181Entity> {
        public CSK181Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new CSK181Model());
        }
}