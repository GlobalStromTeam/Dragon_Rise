package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T3485Model;
import com.redabysslucia.dragonrise_reforge.entities.T3485Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class T3485Renderer extends VehicleRenderer<T3485Entity> {
    public T3485Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new T3485Model());
    }
}
