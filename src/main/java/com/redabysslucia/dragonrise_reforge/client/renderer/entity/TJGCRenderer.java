package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.TJGCEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * tjgc 渲染器：继承 {@link DragonriseVehicleRenderer} 以支持涂装自带模型 / 发光贴图
 * （涂装 skin 的模型与发光贴图：models/bedrock/vehicle/tjgc_skin.geo.json、
 * textures/entity/tjgc_skin.png、textures/entity/tjgc_skin_glow.png）。
 *
 * <p><b>发光贴图规则</b>
 * <ul>
 *   <li>默认涂装、{@code black}、{@code black1}：统一用 {@code textures/entity/glow/tjgc_glow.png}。
 *       这三者都没有自己的 {@code *_glow.png}，按父类命名约定会取不到发光贴图，所以在这里兜底。</li>
 *   <li>{@code skin} 涂装：自带 {@code tjgc_skin_glow.png}，走父类逻辑，不并入统一发光。</li>
 * </ul>
 */
public class TJGCRenderer extends DragonriseVehicleRenderer<TJGCEntity> {

    /** 默认 / black / black1 共用的发光贴图。 */
    private static final ResourceLocation TJGC_GLOW =
            new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/glow/tjgc_glow.png");

    /** skin 涂装保持自己的发光贴图，不套用统一发光。 */
    private static final String SKIN_EXCLUDED = "skin";

    private final Map<VehicleModelInstance, BoneState[][][]> payloadCache = new WeakHashMap<>();

    public TJGCRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getEmissiveTextureLocation(PoseStack poseStack, TJGCEntity entity) {
        if (SKIN_EXCLUDED.equals(entity.getSkinId())) {
            return super.getEmissiveTextureLocation(poseStack, entity);
        }
        return TJGC_GLOW;
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
