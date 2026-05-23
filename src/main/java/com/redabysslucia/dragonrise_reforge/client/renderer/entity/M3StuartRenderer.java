package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M3StuartModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M4A2Model;
import com.redabysslucia.dragonrise_reforge.entities.M3StuartEntity;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M3StuartRenderer extends VehicleRenderer<M3StuartEntity> {
        public M3StuartRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new M3StuartModel());
        }
}
