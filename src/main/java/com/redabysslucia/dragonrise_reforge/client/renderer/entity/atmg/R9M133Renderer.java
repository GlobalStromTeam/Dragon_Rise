package com.redabysslucia.dragonrise_reforge.client.renderer.entity.atmg;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.atmg.R9M133Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 短号 9M133 发射器的渲染：发射筒（模型里的 {@code move_missile} 骨骼）在
 * 「已装填」和「空筒待卸」两种状态下都显示，只有把空筒卸下后才消失。
 * <p>
 * 状态机在服务端：{@link R9M133Entity#ROUND_LOADED} → 开火后 {@link R9M133Entity#ROUND_SPENT_TUBE}
 * （空筒留在架上）→ 潜行+右键卸下 → {@link R9M133Entity#ROUND_EMPTY}（骨骼隐藏）。
 */
public class R9M133Renderer extends GeoVehicleRenderer<R9M133Entity> {

    public R9M133Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(R9M133Entity entity, VehicleModelInstance instance, PoseStack poseStack,
                                        float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        // 以状态机为准（不掺弹匣数量）：
        //   LOADED      → 显示（装填完成的筒）
        //   SPENT_TUBE  → 显示（打完后留在架上的空筒，**打完不立刻消失**）
        //   EMPTY       → 隐藏（无筒）
        // 装填新的一发时状态回到 LOADED，空筒被新筒取代，视觉上不中断。
        var missile = instance.getBone("move_missile");
        if (missile != null) {
            missile.visible = entity.roundState() != R9M133Entity.ROUND_EMPTY;
        }
    }
}
