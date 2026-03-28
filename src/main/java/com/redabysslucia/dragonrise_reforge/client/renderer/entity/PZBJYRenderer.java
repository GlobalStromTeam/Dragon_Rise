package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.PZBJYModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TYPE100Model;
import com.redabysslucia.dragonrise_reforge.entities.PZBJYEntity;
import com.redabysslucia.dragonrise_reforge.entities.TYPE100Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PZBJYRenderer extends VehicleRenderer<PZBJYEntity> {
        public PZBJYRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new PZBJYModel());
        }
}
