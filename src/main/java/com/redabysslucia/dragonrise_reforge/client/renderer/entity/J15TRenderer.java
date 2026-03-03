package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J15TGlowLayer;
import com.redabysslucia.dragonrise_reforge.client.layer.J16GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J15TModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J16Model;
import com.redabysslucia.dragonrise_reforge.entities.J15TEntity;
import com.redabysslucia.dragonrise_reforge.entities.J16Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class J15TRenderer extends VehicleRenderer<J15TEntity> {
        public J15TRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager,new J15TModel());
            this.addRenderLayer(new J15TGlowLayer(this));

        }
}
