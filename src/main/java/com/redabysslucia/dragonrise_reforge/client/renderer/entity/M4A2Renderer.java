package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M4A2Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.PANZER4Model;
import com.redabysslucia.dragonrise_reforge.entities.M4A2Entity;
import com.redabysslucia.dragonrise_reforge.entities.PANZER4Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M4A2Renderer extends VehicleRenderer<M4A2Entity> {
        public M4A2Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new M4A2Model());
        }
}
