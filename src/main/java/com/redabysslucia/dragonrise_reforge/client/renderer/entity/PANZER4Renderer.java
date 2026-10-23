package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.PANZER4Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PANZER4Renderer extends AnimatedTrackedVehicleRenderer<PANZER4Entity> {
        public PANZER4Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, "panzer4");
        }
}
