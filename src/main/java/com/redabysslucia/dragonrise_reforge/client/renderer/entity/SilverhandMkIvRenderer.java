package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.SilverhandMkIvEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 银手 - Mk. IV 渲染器。
 * <p>
 * 车体 68mm 副炮的独立瞄准走 <b>SuperbWarfare 原生绑定骨骼机制</b>，不需要这里写代码：
 * 武器数据里声明 {@code BoundBonesYaw: ["move_hullGunYaw"]} /
 * {@code BoundBonesPitch: ["move_hullGunPitch"]}，
 * SBW 的 {@code GeoVehicleRenderer.transformCustomModelPart} 会在二号位有人时，
 * 按乘员视线（{@code Directions: ["Passenger"]}）与
 * {@code DefaultBarrelDirection} 的差值分别旋转偏航 / 俯仰骨骼。
 * <p>
 * ⚠️ 骨骼名必须以 {@code move_} 开头：SBM v2 烘焙时只把白名单骨骼保留为运行时骨骼
 * （SBW {@code VehicleModelReloadListener.PATTERNS}：{@code ^move_.*}、{@code ^turret$} 等），
 * 其余骨骼会被折进静态几何，{@code instance.getBone(name)} 返回 null，绑定骨骼会静默失效。
 */
public class SilverhandMkIvRenderer extends GeoVehicleRenderer<SilverhandMkIvEntity> {
    public SilverhandMkIvRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }
}
