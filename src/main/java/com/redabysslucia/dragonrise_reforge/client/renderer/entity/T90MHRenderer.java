package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T80Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T90MHModel;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;
import com.redabysslucia.dragonrise_reforge.entities.T90MHEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class T90MHRenderer extends VehicleRenderer<T90MHEntity> {
        public T90MHRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new T90MHModel());
        }
}
