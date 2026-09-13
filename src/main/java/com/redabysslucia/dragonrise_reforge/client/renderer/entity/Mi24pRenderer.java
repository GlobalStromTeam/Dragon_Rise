package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.Mi24pEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Mi-24P 渲染器：与 Mi-24V 一致，接入 {@link RotorVehicleRenderer} 的旋翼逻辑
 * （move_propeller 绕 Y、move_tailpropeller 绕 X×6，随同步的 PropellerRot 转动）。
 */
public class Mi24pRenderer extends RotorVehicleRenderer<Mi24pEntity> {
    public Mi24pRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }
}
