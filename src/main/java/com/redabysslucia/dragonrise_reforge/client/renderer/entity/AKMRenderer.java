package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AA625EModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AKMModel;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import com.redabysslucia.dragonrise_reforge.entities.AKMEEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AKMRenderer extends VehicleRenderer<AKMEEntity> {
        public AKMRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new AKMModel());
        }
}