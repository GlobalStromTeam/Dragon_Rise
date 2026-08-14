package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;


import com.redabysslucia.dragonrise_reforge.entities.utils.TestShipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TestShipRenderer extends GeoVehicleRenderer<TestShipEntity> {
        public TestShipRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager);
        }
}
