package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.ZBL08Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * 外置双联导弹发射架（move_missile3/move_missile4 骨骼）：
 * 导弹装填好后就一直挂在架上（与当前选中武器无关）。
 * 满弹 2 发时两枚都在架；发射一枚隐藏一枚（先发射的是第二枚 move_missile4）：
 * ammo == 2 → 两枚显示；ammo == 1 → 只剩第一枚 move_missile3；ammo == 0 → 发射架空（装填中）。
 */
public class ZBL08Renderer extends GeoVehicleRenderer<ZBL08Entity> {

    public ZBL08Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(ZBL08Entity entity, VehicleModelInstance instance,
                                         PoseStack poseStack, float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        boolean show3 = false, show4 = false;
        var gunData = entity.getGunData("Missile");
        if (gunData != null) {
            int ammo = gunData.ammo.get();
            show3 = ammo >= 1;  // 第一枚：还剩至少 1 发时在架
            show4 = ammo >= 2;  // 第二枚（先发射）：满弹时在架
        }

        BoneState missile3 = instance.getBone("move_missile3");
        if (missile3 != null) {
            missile3.visible = show3;
        }
        BoneState missile4 = instance.getBone("move_missile4");
        if (missile4 != null) {
            missile4.visible = show4;
        }
    }
}
