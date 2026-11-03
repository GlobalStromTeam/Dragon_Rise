package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.M3A3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/** Animated running belts and the rack pose shared with projectile/OBB transforms. */
public class M3A3Renderer extends AnimatedTrackedVehicleRenderer<M3A3Entity> {
    public M3A3Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, "m3a3");
    }

    @Override
    public void transformCustomModelPart(M3A3Entity entity, VehicleModelInstance instance,
                                         PoseStack poseStack, float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
        var tow = instance.getBone("tow");
        if (tow != null) {
            tow.rotation.rotationZ(entity.getMissileRackRoll() * Mth.DEG_TO_RAD);
        }

        // 附加装甲（TUSK）：未安装时隐藏 move_tusk 骨骼（默认隐藏，靠套件安装后显示）
        var tusk = instance.getBone("move_tusk");
        if (tusk != null) {
            tusk.visible = entity.hasAddonArmor();
        }
    }
}
