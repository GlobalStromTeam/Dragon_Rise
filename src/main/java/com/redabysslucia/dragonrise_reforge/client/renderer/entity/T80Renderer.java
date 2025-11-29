package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T80Model;
import com.redabysslucia.dragonrise_reforge.entities.T80Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class T80Renderer extends VehicleRenderer<T80Entity> {
        public T80Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new T80Model());
        }
}
