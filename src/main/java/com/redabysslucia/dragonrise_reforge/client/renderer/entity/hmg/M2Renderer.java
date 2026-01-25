package com.redabysslucia.dragonrise_reforge.client.renderer.entity.hmg;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.hmg.M2Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.hmg.qjz89Model;
import com.redabysslucia.dragonrise_reforge.entities.hmg.M2Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.qjz89Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M2Renderer extends VehicleRenderer<M2Entity> {
    public M2Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new M2Model());
    }
}
