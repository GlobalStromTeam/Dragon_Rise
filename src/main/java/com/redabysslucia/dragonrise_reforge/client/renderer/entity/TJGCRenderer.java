package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * tjgc 渲染器：继承 {@link DragonriseVehicleRenderer} 以支持涂装自带模型 / 发光贴图
 * （涂装 skin 的模型与发光贴图：models/bedrock/vehicle/tjgc_skin.geo.json、
 * textures/entity/tjgc_skin.png、textures/entity/tjgc_skin_glow.png）。
 */
public class TJGCRenderer extends DragonriseVehicleRenderer<TJGCEntity> {
    private final Map<VehicleModelInstance, BoneState[][][]> payloadCache = new WeakHashMap<>();

    public TJGCRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(TJGCEntity entity, VehicleModelInstance instance,
                                         PoseStack poseStack, float entityYaw, float partialTicks) {
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);
        BoneState[][][] groups = payloadCache.computeIfAbsent(instance, TJGCRenderer::bindPayloads);
        for (int group = 0; group < groups.length; group++) {
            var data = entity.getGunData(TJGCPayloadSlots.WEAPONS[group]);
            int ammo = data == null || entity.getHealth() < 0 ? 0 : data.ammo.get();
            int fireIndex = data == null ? 0 : data.fireIndex.get();
            boolean bound = data != null && data.get(GunProp.SHOOT_POS).getBoundUpWithAmmoAmount();
            BoneState[][] slots = groups[group];
            for (int slot = 0; slot < slots.length; slot++) {
                boolean visible = TJGCPayloadSlots.isVisible(slot, ammo, slots.length, fireIndex, bound);
                // Animated child meshes remain separate after baking; hide the
                // entire round, including its independently animated nose/fins.
                for (BoneState bone : slots[slot]) {
                    bone.visible = visible;
                }
            }
        }
    }

    private static BoneState[][][] bindPayloads(VehicleModelInstance instance) {
        String[][][] names = TJGCPayloadSlots.BONES;
        BoneState[][][] result = new BoneState[names.length][][];
        for (int group = 0; group < names.length; group++) {
            result[group] = new BoneState[names[group].length][];
            for (int slot = 0; slot < names[group].length; slot++) {
                var bones = new ArrayList<BoneState>();
                for (String name : names[group][slot]) {
                    BoneState bone = instance.getBone(name);
                    if (bone != null) {
                        bones.add(bone);
                    }
                }
                result[group][slot] = bones.toArray(BoneState[]::new);
            }
        }
        return result;
    }
}
