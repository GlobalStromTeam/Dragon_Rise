package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.VT4A1Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTQ15Model;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTQ15Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ZTQ15Renderer extends VehicleRenderer<ZTQ15Entity> {
        public ZTQ15Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new ZTQ15Model());
        }
}
