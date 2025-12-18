package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Project640Model;
import com.redabysslucia.dragonrise_reforge.entities.Project640Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Project640Renderer extends VehicleRenderer<Project640Entity> {
        public Project640Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new Project640Model());
        }
}
