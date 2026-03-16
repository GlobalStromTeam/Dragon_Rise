package com.redabysslucia.dragonrise_reforge.client.renderer.entity.atmg;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.atmg.HJ8Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.hmg.DSHKModel;
import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.DSHKEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HJ8Renderer extends VehicleRenderer<HJ8Entity> {
    public HJ8Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HJ8Model());
    }
}
