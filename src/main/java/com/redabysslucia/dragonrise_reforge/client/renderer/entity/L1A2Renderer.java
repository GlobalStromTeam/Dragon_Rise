package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.L1A2Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZSU234Model;
import com.redabysslucia.dragonrise_reforge.entities.L1A2Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZSU234EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class L1A2Renderer extends VehicleRenderer<L1A2Entity> {
        public L1A2Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new L1A2Model());
        }
}