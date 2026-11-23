package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.Leopard2a7Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Leopard 2A7：套用 Leopard 2A4 的独立左右履带方案（VehicleTrackAnimation）。
 * 皮带节骨骼 trackMovL/R0..59 + trackRotL/R0..59 由 tools/gen_leopard2a7.py 生成，
 * 路径数据见 tracks/leopard2a7.json。
 */
public class Leopard2a7Renderer extends AnimatedTrackedVehicleRenderer<Leopard2a7Entity> {
    public Leopard2a7Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, "leopard2a7");
    }
}
