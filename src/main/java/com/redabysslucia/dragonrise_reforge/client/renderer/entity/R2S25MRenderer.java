package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M10BookerModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.R2S25MModel;
import com.redabysslucia.dragonrise_reforge.entities.M10BookerEntity;
import com.redabysslucia.dragonrise_reforge.entities.R2S25MEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class R2S25MRenderer extends VehicleRenderer<R2S25MEntity> {
    public R2S25MRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager,new R2S25MModel());
    }
}