package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z10AModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Z10MEModel;
import com.redabysslucia.dragonrise_reforge.entities.Z10AEntity;
import com.redabysslucia.dragonrise_reforge.entities.Z10MEEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Z10ARenderer extends VehicleRenderer<Z10AEntity> {
        public Z10ARenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new Z10AModel());
        }
}
