package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.baked.BakedGeometryChunk;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参照官方 Mi28Renderer / Ah6Renderer 的螺旋桨/尾桨动画实现。
 * 本包所有直升机的旋翼骨骼均已加 move_ 前缀，按骨骼名自适应旋转：
 *  - 标准型：move_propeller（主旋翼，绕 Y）+ move_tailPropeller（尾桨，绕 X * 6）
 *  - KA-50：move_propeller0 / move_propeller1（共轴反转双旋翼，绕 Y）
 *  - AC-130：move_propeller1 ~ move_propeller4（四台发动机，绕 Z）
 *  - SD-905：move_propeller / move_propeller2（两侧双旋翼，绕 Y 反转，无尾桨）
 *  - NH-90：move_propeller2 为桨毂父骨骼，只转桨叶 move_propeller（绕 Y）+ move_tailPropeller
 *  - Mi-24V：move_propeller（桨叶，绕 Y）+ move_tailpropeller（小写尾桨，绕 X * 6）
 *
 * 三个只打一次的自检日志，用来区分「不转」的真实原因：
 *  1. 模型里完全没有旋翼骨骼 -> 打印烘焙后真实存在的骨骼名（名字没命中 SBW 保留正则时几何会被折叠走）；
 *  2. 骨骼在、但它身上没几何 -> 打印几何实际挂在哪个分块（桨叶还留在 body 里就是这种）；
 *  3. 骨骼和几何都对、但 PropellerRot 三秒不动 -> 引擎没启动或载具能量为 0。
 */
public class RotorVehicleRenderer<T extends VehicleEntity> extends GeoVehicleRenderer<T> {

    /** 主旋翼候选名（按优先级）。 */
    private static final String[] MAIN_ROTOR = {
            "move_propeller", "propeller", "move_rotor", "rotor", "move_mainRotor", "mainRotor"
    };
    /** 尾桨候选名（按优先级，含大小写变体）。 */
    private static final String[] TAIL_ROTOR = {
            "move_tailPropeller", "move_tailpropeller", "tailPropeller", "tailpropeller",
            "move_tailRotor", "move_tailrotor", "tailRotor"
    };

    private static final Set<String> WARNED_NO_BONE = ConcurrentHashMap.newKeySet();
    private static final Set<String> WARNED_EMPTY = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> WARNED_STUCK = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, float[]> ROT_MEMO = new HashMap<>();

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

        BoneState propeller = findFirst(instance, MAIN_ROTOR);
        BoneState tail = findFirst(instance, TAIL_ROTOR);

        if (propeller == null && tail == null) {
            warnNoBone(entity, instance);
            return;
        }

        if (propeller != null) {
            propeller.rotation.rotateY(-rot);
            warnIfEmptyGeometry(entity, instance, propeller, "move_propeller");
        }

        if (tail != null) {
            tail.rotation.rotateX(6f * rot);
            warnIfEmptyGeometry(entity, instance, tail, "move_tailPropeller");
        } else {
            // 无尾桨 -> 两侧双旋翼（SD-905），反转
            BoneState p2 = instance.getBone("move_propeller2");
            if (p2 != null) {
                p2.rotation.rotateY(rot);
            }
        }

        warnIfStuck(entity, rot, propeller != null ? propeller.name() : tail.name());
    }

    /** 找不到任何旋翼骨骼时，一次性把烘焙后真实存在的骨骼名打出来。 */
    private static void warnNoBone(VehicleEntity entity, VehicleModelInstance instance) {
        String key = String.valueOf(entity.getType());
        if (!WARNED_NO_BONE.add(key)) return;
        Dragonrise_reforge.LOGGER.warn("[rotor] model of {} has no rotor bone (looked for {}). "
                        + "Bones present after baking = {}. If this list lacks any move_/propeller name, "
                        + "the geometry got folded into a parent bone: bone names must match SBW preserved "
                        + "patterns (^move_.*, ^wheel[LR].*, ^turret$, ^base$ ...)",
                key, String.join("/", MAIN_ROTOR), boneNames(instance));
    }

    /**
     * 骨骼在、但它身上没挂几何 —— 「分了骨骼却不转」最常见的一种：
     * 桨叶还留在 body 或别的骨骼的 poly_mesh 里，转这根空骨骼当然没反应。
     */
    private static void warnIfEmptyGeometry(VehicleEntity entity, VehicleModelInstance instance,
                                            BoneState bone, String expectName) {
        String key = String.valueOf(entity.getType()) + "#" + bone.name();
        if (WARNED_EMPTY.contains(key)) return;
        int idx = bone.index();
        BakedGeometryChunk biggest = null;
        for (BakedGeometryChunk chunk : instance.baseModel().chunks()) {
            if (chunk.attachBoneIndex() == idx && chunk.hasVertices()) return;
            if (biggest == null || chunk.quadCount() > biggest.quadCount()) biggest = chunk;
        }
        WARNED_EMPTY.add(key);
        Dragonrise_reforge.LOGGER.warn("[rotor] bone '{}' exists but carries no geometry "
                        + "(renderer needs '{}'). Biggest geometry chunk is attached to bone index {}, "
                        + "sourceBones={} -> move the blade poly_mesh onto '{}' "
                        + "(tools/ch53e_import/fix_rotor_bones.py can do it automatically)",
                bone.name(), expectName,
                biggest == null ? "(none)" : biggest.attachBoneIndex(),
                biggest == null ? "(none)" : String.join(",", biggest.sourceBones()),
                expectName);
    }

    /** 骨骼命中了但 propellerRot 一直不动 —— 引擎没启动，或能量为 0。 */
    private static void warnIfStuck(VehicleEntity entity, float rot, String boneName) {
        UUID id = entity.getUUID();
        float[] memo = ROT_MEMO.get(id);
        long now = System.currentTimeMillis();
        if (memo == null) {
            ROT_MEMO.put(id, new float[]{rot, now});
            return;
        }
        if (Math.abs(memo[0] - rot) > 1.0E-4f) {
            memo[0] = rot;
            memo[1] = now;
            WARNED_STUCK.remove(id);
            return;
        }
        if (now - memo[1] > 3000L && WARNED_STUCK.add(id)) {
            int energy = entity.getEnergy();
            int maxEnergy = entity.getMaxEnergy();
            String hint = energy <= 0
                    ? "energy is 0: SBW force-stops the engine while energy<=energyCost, so W does nothing. "
                      + "Refill with /data merge entity @e[limit=1,type=...] {Energy:" + Math.max(maxEnergy, 1) + "}"
                    : "engine not started: sit in the pilot seat and hold W (up=forwardInputDown) until power rises";
            Dragonrise_reforge.LOGGER.warn("[rotor] bone {} found, but PropellerRot has not changed for 3s (={}). "
                            + "energy={}/{}; {}",
                    boneName, rot, energy, maxEnergy, hint);
        }
    }

    private static String boneNames(VehicleModelInstance instance) {
        StringBuilder sb = new StringBuilder();
        for (BoneState bone : instance.getBoneIndexes()) {
            if (bone == null) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(bone.name());
        }
        return sb.length() == 0 ? "(empty)" : sb.toString();
    }

    private static BoneState findFirst(VehicleModelInstance instance, String[] candidates) {
        for (String name : candidates) {
            BoneState bone = instance.getBone(name);
            if (bone != null) return bone;
        }
        return null;
    }

    private static void rotateZ(VehicleModelInstance instance, String boneName, float angle) {
        BoneState bone = instance.getBone(boneName);
        if (bone != null) {
            bone.rotation.rotateZ(angle);
        }
    }
}
