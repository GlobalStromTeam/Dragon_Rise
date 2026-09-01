package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * 参照官方 Mi28Renderer / Ah6Renderer 的螺旋桨/尾桨动画实现。
 * 本包所有直升机的旋翼骨骼均已加 move_ 前缀，按骨骼名自适应旋转：
 *  - 标准型：move_propeller（主旋翼，绕 Y）+ move_tailPropeller（尾桨，绕 X ×6）
 *  - KA-50：move_propeller0 / move_propeller1（共轴反转双旋翼，绕 Y）
 *  - AC-130：move_propeller1 ~ move_propeller4（四台发动机，绕 Z）
 *  - SD-905：move_propeller / move_propeller2（两侧双旋翼，绕 Y 反转，无尾桨）
 *  - NH-90：move_propeller2 为桨毂父骨骼，只转桨叶 move_propeller（绕 Y）+ move_tailPropeller
 *  - Mi-24V：move_propeller（桨叶，绕 Y）+ move_tailpropeller（小写尾桨，绕 X ×6）
 */
public class RotorVehicleRenderer<T extends VehicleEntity> extends GeoVehicleRenderer<T> {

        public RotorVehicleRenderer(EntityRendererProvider.Context renderManager) {
                super(renderManager);
        }

        @Override
        public void transformCustomModelPart(T entity, VehicleModelInstance instance, PoseStack poseStack,
                                             float entityYaw, float partialTicks) {
                super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

                if (entity.isWreck()) return;

                float rot = Mth.lerp(partialTicks, entity.getPropellerRotO(), entity.getPropellerRot());

                // KA-50 共轴反转双旋翼
                BoneState p0 = instance.getBone("move_propeller0");
                BoneState p1 = instance.getBone("move_propeller1");
                if (p0 != null && p1 != null) {
                        p0.rotation.rotateY(-rot);
                        p1.rotation.rotateY(rot);
                        return;
                }

                // AC-130 四台发动机
                if (instance.getBone("move_propeller3") != null) {
                        rotateZ(instance, "move_propeller1", rot);
                        rotateZ(instance, "move_propeller2", rot);
                        rotateZ(instance, "move_propeller3", rot);
                        rotateZ(instance, "move_propeller4", rot);
                        return;
                }

                // 主旋翼
                BoneState propeller = instance.getBone("move_propeller");
                if (propeller != null) {
                        propeller.rotation.rotateY(-rot);
                }

                // 尾桨（大小写两种命名）
                BoneState tail = instance.getBone("move_tailPropeller");
                if (tail == null) {
                        tail = instance.getBone("move_tailpropeller");
                }
                if (tail != null) {
                        tail.rotation.rotateX(6f * rot);
                } else {
                        // 无尾桨 -> 两侧双旋翼（SD-905），反转
                        BoneState p2 = instance.getBone("move_propeller2");
                        if (p2 != null) {
                                p2.rotation.rotateY(rot);
                        }
                }
        }

        private static void rotateZ(VehicleModelInstance instance, String boneName, float angle) {
                BoneState bone = instance.getBone(boneName);
                if (bone != null) {
                        bone.rotation.rotateZ(angle);
                }
        }
}
