package com.redabysslucia.dragonrise_reforge.client.renderer.entity.hmg;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.MK19Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.hmg.qjz89Model;
import com.redabysslucia.dragonrise_reforge.entities.MK19Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.qjz89Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class qjz89Renderer extends VehicleRenderer<qjz89Entity> {
    public qjz89Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new qjz89Model());
    }
}
