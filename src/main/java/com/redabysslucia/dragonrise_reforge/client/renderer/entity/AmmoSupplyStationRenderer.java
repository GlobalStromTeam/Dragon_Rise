package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.AmmoSupplyStationEntity;
import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.AmmoSupplyStationModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AmmoSupplyStationRenderer extends VehicleRenderer<AmmoSupplyStationEntity> {
    public AmmoSupplyStationRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AmmoSupplyStationModel());
    }
}
