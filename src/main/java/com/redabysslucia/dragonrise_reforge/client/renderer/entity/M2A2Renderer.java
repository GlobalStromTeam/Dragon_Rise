package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M2A2Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M4A2Model;
import com.redabysslucia.dragonrise_reforge.entities.M2A2Entity;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M2A2Renderer extends VehicleRenderer<M2A2Entity> {
        public M2A2Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new M2A2Model());
        }
}
