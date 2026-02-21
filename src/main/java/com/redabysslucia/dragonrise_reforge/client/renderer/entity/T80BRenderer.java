package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T80BModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T80Model;
import com.redabysslucia.dragonrise_reforge.entities.T80BEntity;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class T80BRenderer extends VehicleRenderer<T80BEntity> {
        public T80BRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new T80BModel());
        }
}
