package com.redabysslucia.dragonrise_reforge.client.renderer.entity.atmg;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.atmg.HJ8Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.atmg.R9M133Model;
import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import com.redabysslucia.dragonrise_reforge.entities.atmg.R9M133Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class R9M133Renderer extends VehicleRenderer<R9M133Entity> {
    public R9M133Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new R9M133Model());
    }
}
