package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.NPDS114Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ59AModel;
import com.redabysslucia.dragonrise_reforge.entities.NPDS114Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ59AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NPDS114Renderer extends VehicleRenderer<NPDS114Entity> {
        public NPDS114Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new NPDS114Model());
        }
}
