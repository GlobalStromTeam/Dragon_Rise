package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AA625EModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZSU234Model;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZSU234EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZSU234Renderer extends VehicleRenderer<ZSU234EEntity> {
        public ZSU234Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZSU234Model());
        }
}