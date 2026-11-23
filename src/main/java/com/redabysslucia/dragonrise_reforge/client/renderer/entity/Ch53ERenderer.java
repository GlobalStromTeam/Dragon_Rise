package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.redabysslucia.dragonrise_reforge.entities.Ch53EEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * CH-53E 渲染器：复用 RotorVehicleRenderer 的旋翼逻辑。
 * 模型骨骼名必须是 move_propeller（主旋翼，绕 Y）与 move_tailPropeller（尾桨，绕 X）。
 */
public class Ch53ERenderer extends RotorVehicleRenderer<Ch53EEntity> {

    public Ch53ERenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }
}
