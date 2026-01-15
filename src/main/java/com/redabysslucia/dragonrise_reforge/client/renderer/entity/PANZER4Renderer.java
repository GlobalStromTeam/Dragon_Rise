package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.PANZER4Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.VT4A1Model;
import com.redabysslucia.dragonrise_reforge.entities.PANZER4Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PANZER4Renderer extends VehicleRenderer<PANZER4Entity> {
        public PANZER4Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new PANZER4Model());
        }
}
