package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.S2S38GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.special.CyborgTankGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.S2S38Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.special.CyborgTankModel;
import com.redabysslucia.dragonrise_reforge.entities.S2S38Entity;
import com.redabysslucia.dragonrise_reforge.entities.special.CyborgTankEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CyborgTankRenderer extends VehicleRenderer<CyborgTankEntity> {
        public CyborgTankRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new CyborgTankModel());
                this.addRenderLayer(new CyborgTankGlowLayer(this));
        }
}
