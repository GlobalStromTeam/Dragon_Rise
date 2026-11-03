package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.Zbd05Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * ZBD05 车顶外置双联导弹发射架（{@code missile3} / {@code missile4} 骨骼）：
 * 这两个骨骼与 ZBL08 的 {@code move_missile3} / {@code move_missile4} 是同一套弹体几何，
 * 因此显示/隐藏状态机沿用 {@link ZBL08Renderer} 的写法——只按同步过来的弹药数判定，
 * 与当前选中的武器无关。
 * <p>
 * 满弹 2 发时两枚都在架；发射一枚隐藏一枚（先发射的是第二枚 {@code missile4}，
 * 对应 zbd05.json 里 Missile 的 {@code ShootPos.Positions} 索引 0，即 -x 侧）：
 * <ul>
 *   <li>{@code ammo == 2} → 两枚显示</li>
 *   <li>{@code ammo == 1} → 只剩第一枚 {@code missile3}</li>
 *   <li>{@code ammo == 0} → 发射架空（装填中，18 秒后重新出现两枚）</li>
 * </ul>
 * 骨骼找不到时（LOD 模型或模型未加载完）直接跳过，不抛异常。
 */
public class Zbd05Renderer extends AnimatedTrackedVehicleRenderer<Zbd05Entity> {

    public Zbd05Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, "zbd05");
    }

    @Override
    public void transformCustomModelPart(Zbd05Entity entity, VehicleModelInstance instance,
                                         PoseStack poseStack, float entityYaw, float partialTicks) {
        // super 里跑履带动画驱动，必须保留
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        boolean show3 = false, show4 = false;
        var gunData = entity.getGunData("Missile");
        if (gunData != null) {
            int ammo = gunData.ammo.get();
            show3 = ammo >= 1;  // 第一枚：还剩至少 1 发时在架
            show4 = ammo >= 2;  // 第二枚（先发射）：满弹时在架
        }

        BoneState missile3 = instance.getBone("move_missile2");
        if (missile3 != null) {
            missile3.visible = show3;
        }
        BoneState missile4 = instance.getBone("move_missile3");
        if (missile4 != null) {
            missile4.visible = show4;
        }
    }
}
