package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.layer.J20GlowLayer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.J20Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.JAS39EModel;
import com.redabysslucia.dragonrise_reforge.entities.J20Entity;
import com.redabysslucia.dragonrise_reforge.entities.utils.JAS39EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class JAS39ERenderer extends VehicleRenderer<JAS39EEntity> {
        public JAS39ERenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager, new JAS39EModel());
        }
}
