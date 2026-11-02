package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.redabysslucia.dragonrise_reforge.entities.DgswMkL2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * DGSW MK L2 渲染器：沿用 SBW 的载具渲染（与双尾蝎一致，不做遮挡特例）。
 * <p>
 * {@code barrel} 骨骼的俯仰由 SBW 的 {@code GeoVehicleRenderer} 自动驱动
 * （读 {@code turretXRot} 并按数据 {@code TurretPitchRange} 夹住），不需要自定义骨骼代码。
 * <p>
 * 唯一要接的一处：实体把 {@code getPitch()} 改成了"机体俯仰 + 炮管俯角"（那是给遥控相机用的），
 * 而骨架的姿态也用同一个 {@code getPitch()}，直接沿用会让整台无人机跟着炮管一起栽下去。
 * 所以这里每帧把骨架俯仰改回 {@code dr$bodyPitch()}：<b>机身永远水平，只有炮管动</b>。
 */
public class DgswMkL2Renderer extends GeoVehicleRenderer<DgswMkL2Entity> {

    public DgswMkL2Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void tickVariables(DgswMkL2Entity entity, float entityYaw, float partialTicks) {
        super.tickVariables(entity, entityYaw, partialTicks);
        // super 读的是 entity.getPitch()（含炮管俯角），这里换回纯机体俯仰
        this.setPitch(entity.dr$bodyPitch(partialTicks));
    }
}
