package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * tjgc 渲染器：继承 {@link DragonriseVehicleRenderer} 以支持涂装自带模型 / 发光贴图
 * （涂装 skin 的模型与发光贴图：models/bedrock/vehicle/tjgc_skin.geo.json、
 * textures/entity/tjgc_skin.png、textures/entity/tjgc_skin_glow.png）。
 */
public class TJGCRenderer extends DragonriseVehicleRenderer<TJGCEntity> {
        public TJGCRenderer(EntityRendererProvider.Context renderManager) {

            super(renderManager);
        }
}
