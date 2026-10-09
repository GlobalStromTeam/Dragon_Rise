package com.redabysslucia.dragonrise_reforge.client.renderer.entity.atmg;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 红箭8 发射器的渲染：发射筒（模型里的 {@code move_missile} 骨骼）只在「已装填」时显示。
 * <p>
 * 状态机在服务端：{@link HJ8Entity#ROUND_LOADED}（筒+弹在）→ 开火后自动跳到
 * {@link HJ8Entity#ROUND_EMPTY}（发射筒被抛弃，骨骼隐藏），空筒不会留在架上。
 */
public class HJ8Renderer extends GeoVehicleRenderer<HJ8Entity> {

    public HJ8Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(HJ8Entity entity, VehicleModelInstance instance, PoseStack poseStack,
                                         float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        // 判定以弹匣里的实弹数为准（与 ZBL08Renderer 同款写法，客户端读的是同步过来的 gun data），
        // 再叠加状态机：这样即使状态因任何原因滞后，只要弹匣空了，筒就一定隐藏。
        var gunData = entity.getGunData("Missile");
        boolean hasRound = gunData != null && gunData.ammo.get() > 0;

        var missile = instance.getBone("move_missile");
        if (missile != null) {
            missile.visible = hasRound && entity.roundState() == HJ8Entity.ROUND_LOADED;
        }
    }
}
