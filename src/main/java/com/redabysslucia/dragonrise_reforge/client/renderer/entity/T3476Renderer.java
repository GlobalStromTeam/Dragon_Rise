package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T3476Model;
import com.redabysslucia.dragonrise_reforge.entities.T3476Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class T3476Renderer extends VehicleRenderer<T3476Entity> {
        public T3476Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new T3476Model());
        }
}