package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.Pak40Entity;
import com.redabysslucia.dragonrise_reforge.client.model.entity.Pak40Model;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Pak40Renderer extends VehicleRenderer<Pak40Entity> {
        public Pak40Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new Pak40Model());
        }
}
