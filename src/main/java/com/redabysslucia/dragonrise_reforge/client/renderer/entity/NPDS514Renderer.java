package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.NPDS114Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.NPDS514Model;
import com.redabysslucia.dragonrise_reforge.entities.NPDS114Entity;
import com.redabysslucia.dragonrise_reforge.entities.NPDS514Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NPDS514Renderer extends VehicleRenderer<NPDS514Entity> {
        public NPDS514Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new NPDS514Model());
        }
}
