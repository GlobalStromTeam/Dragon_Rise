package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;

import com.redabysslucia.dragonrise_reforge.client.model.entity.TestShipModel;

import com.redabysslucia.dragonrise_reforge.entities.utils.TestShipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TestShipRenderer extends VehicleRenderer<TestShipEntity> {
        public TestShipRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new TestShipModel());
        }
}
