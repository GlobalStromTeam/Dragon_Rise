package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.HYR0GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J8GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.HYR0Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AModel;
import com.redabysslucia.dragonrise_reforge.entities.HYR0Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HYR0Renderer extends VehicleRenderer<HYR0Entity> {
        public HYR0Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new HYR0Model());
                this.addRenderLayer(new HYR0GlowLayer(this));
        }
}
