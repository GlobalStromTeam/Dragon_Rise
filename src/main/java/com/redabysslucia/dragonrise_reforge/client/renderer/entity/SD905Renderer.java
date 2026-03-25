package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.SD905Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.TestShipModel;
import com.redabysslucia.dragonrise_reforge.entities.SD905Entity;
import com.redabysslucia.dragonrise_reforge.entities.utils.TestShipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SD905Renderer extends VehicleRenderer<SD905Entity> {
        public SD905Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new SD905Model());
        }
}
