package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.Zsd05Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Zsd05Renderer extends AnimatedTrackedVehicleRenderer<Zsd05Entity> {
    public Zsd05Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, "zsd05");
    }
}
