package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J16GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.S2S38GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J16Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.S2S38Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.VT4A1Model;
import com.redabysslucia.dragonrise_reforge.entities.S2S38Entity;
import com.redabysslucia.dragonrise_reforge.entities.VT4A1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class S2S38Renderer extends VehicleRenderer<S2S38Entity> {
        public S2S38Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new S2S38Model());
                this.addRenderLayer(new S2S38GlowLayer(this));
        }
}
