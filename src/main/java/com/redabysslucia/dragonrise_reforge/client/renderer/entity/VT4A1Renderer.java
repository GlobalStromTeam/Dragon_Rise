package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.VT4A1Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AModel;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class VT4A1Renderer extends VehicleRenderer<VT4A1Entity> {
        public VT4A1Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new VT4A1Model());
        }
}
