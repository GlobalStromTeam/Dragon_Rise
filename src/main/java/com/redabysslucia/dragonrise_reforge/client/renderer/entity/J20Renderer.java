package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J10Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20Model;
import com.redabysslucia.dragonrise_reforge.entities.J10Entity;
import com.redabysslucia.dragonrise_reforge.entities.J20Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J20Renderer extends VehicleRenderer<J20Entity> {
        public J20Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new J20Model());
        }
}
