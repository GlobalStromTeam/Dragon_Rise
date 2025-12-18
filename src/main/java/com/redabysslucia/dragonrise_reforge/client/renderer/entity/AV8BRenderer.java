package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J20VTOLGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AV8BModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20VTOLModel;
import com.redabysslucia.dragonrise_reforge.entities.AV8BEntity;
import com.redabysslucia.dragonrise_reforge.entities.J20VTOLEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AV8BRenderer extends VehicleRenderer<AV8BEntity> {
        public AV8BRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new AV8BModel());
        }
}
