package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.hyr0.HYR0TrackCurves;
import com.redabysslucia.dragonrise_reforge.entities.HYR0Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * HYR0 履带：**每侧两条独立履带**（前组、后组），所以要覆盖卓越前线的单链驱动。
 * <p>
 * 卓越前线的 {@code GeoVehicleRenderer.transformCustomModelPart} 按骨骼名 {@code track(Mov|Rot)[LR]\d+}
 * 驱动履带，公式是 {@code t = wrap(leftTrack + distance * index)} —— 每侧只有一条曲线，46 个链节全部排在
 * 同一条环上；HYR0 每侧是"前组 18 节 + 后组 28 节"两条独立的环，所以这里在 {@code super} 之后
 * 用两条各自的曲线重新排布全部 92 个链节（super 已经放好的位置会被覆盖）。
 * <p>
 * 曲线数据见 {@link HYR0TrackCurves}（由模型轮组几何生成）：
 * <ul>
 *   <li>前环：3 个 Ø1.08 轮，周长 134.3px，18 节</li>
 *   <li>后环：4 个 Ø1.08 轮 + 后上方 Ø0.85 导引轮，周长 201.0px，28 节</li>
 * </ul>
 * 左右两侧几何相同（仅 x 镜像），共用这两条曲线；相位取车轮转角换算成像素
 * （{@code 1.5 * wheelRot * 轮半径}，与车轮骨骼的旋转用同一个值），所以履带与车轮永远同步。
 */
public class HYR0Renderer extends GeoVehicleRenderer<HYR0Entity> {

    /** 履带环的行进方向：+1 = 按曲线 t 增大方向跑（顶边朝 -z），-1 = 反向（实机验证后取 -1） */
    private static final float TRACK_DIRECTION = -1f;
    /** 链节自转（绕 X 轴贴向曲线切线）的符号：+1 = 直接取曲线朝向，-1 = 取相反数 */
    private static final float LINK_ROT_SIGN = 1f;
    /** 与 GeoVehicleRenderer 里车轮骨骼的旋转倍率一致（bone.rotation.rotationX(1.5f * wheelRot)） */
    private static final float WHEEL_SPIN_SCALE = 1.5f;
    /** 牵引轮半径（模型单位，直径 17.28px）—— 履带线速度 = 车轮角速度 × 此半径 */
    private static final float DRIVE_WHEEL_RADIUS = 17.28f / 2f;

    public HYR0Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(HYR0Entity entity, VehicleModelInstance instance, PoseStack poseStack,
                                        float entityYaw, float partialTicks) {
        // 车轮旋转、车体摇晃、炮塔等都交给 super；它顺手按单链放好的履带位置随后会被覆盖
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        TrackBones bones = TrackBones.get(instance);
        if (bones == null) {
            return;     // LOD 模型或骨骼名不匹配时不处理
        }

        // 变树形态：隐藏所有履带块（两块 46 节 × 2 侧）。
        // 履带骨骼在模型里是并列的独立骨骼，不在 root 之下，所以 root 缩放为 0 时它们仍会露出来。
        boolean tree = entity.isTree();
        bones.setVisible(!tree);
        if (tree) {
            return;     // 已隐藏，不必再按曲线摆位
        }

        float leftPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getLeftWheelRot() * DRIVE_WHEEL_RADIUS;
        float rightPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getRightWheelRot() * DRIVE_WHEEL_RADIUS;

        driveLoop(bones.leftMov, bones.leftRot, 0, HYR0TrackCurves.FRONT_LINK_COUNT,
                HYR0TrackCurves.FRONT_LOOP_LENGTH, HYR0TrackCurves.FRONT_Y, HYR0TrackCurves.FRONT_Z,
                HYR0TrackCurves.FRONT_ROT, leftPhase);
        driveLoop(bones.leftMov, bones.leftRot, HYR0TrackCurves.FRONT_LINK_COUNT, HYR0TrackCurves.LINKS_PER_SIDE,
                HYR0TrackCurves.REAR_LOOP_LENGTH, HYR0TrackCurves.REAR_Y, HYR0TrackCurves.REAR_Z,
                HYR0TrackCurves.REAR_ROT, leftPhase);

        driveLoop(bones.rightMov, bones.rightRot, 0, HYR0TrackCurves.FRONT_LINK_COUNT,
                HYR0TrackCurves.FRONT_LOOP_LENGTH, HYR0TrackCurves.FRONT_Y, HYR0TrackCurves.FRONT_Z,
                HYR0TrackCurves.FRONT_ROT, rightPhase);
        driveLoop(bones.rightMov, bones.rightRot, HYR0TrackCurves.FRONT_LINK_COUNT, HYR0TrackCurves.LINKS_PER_SIDE,
                HYR0TrackCurves.REAR_LOOP_LENGTH, HYR0TrackCurves.REAR_Y, HYR0TrackCurves.REAR_Z,
                HYR0TrackCurves.REAR_ROT, rightPhase);
    }

    /**
     * 把 [from, to) 区间的链节按给定环的曲线排布。
     * 环内链节等距（节距 = 环长 / 节数），相位以像素为单位在环上循环。
     */
    private void driveLoop(BoneState[] movBones, BoneState[] rotBones, int from, int to, float loopLength,
                           float[] curveY, float[] curveZ, float[] curveRot, float phase) {
        int count = to - from;
        if (count <= 0) {
            return;
        }
        float pitch = loopLength / count;

        for (int k = 0; k < count; k++) {
            int index = from + k;
            if (index >= movBones.length || movBones[index] == null) {
                continue;
            }

            // 环上位置 → 曲线采样下标（曲线按弧长等分，所以是线性的）
            float t = phase + pitch * k;
            t = ((t % loopLength) + loopLength) % loopLength;
            float u = t / loopLength * (HYR0TrackCurves.SAMPLES - 1);
            int i0 = (int) u;
            int i1 = Math.min(i0 + 1, HYR0TrackCurves.SAMPLES - 1);
            float f = u - i0;

            BoneState mov = movBones[index];
            // 与 super 的写法一致：把枢轴折进 y/z
            float pivotY = 0f;
            float pivotZ = 0f;
            var definition = mov.definition();
            if (definition != null) {
                pivotY = definition.pivotY() * 16f;
                pivotZ = definition.pivotZ() * 16f;
            }
            mov.y = pivotY + Mth.lerp(f, curveY[i0], curveY[i1]);
            mov.z = pivotZ + Mth.lerp(f, curveZ[i0], curveZ[i1]);

            if (rotBones != null && index < rotBones.length && rotBones[index] != null) {
                rotBones[index].rotation.rotationX(LINK_ROT_SIGN * Mth.lerp(f, curveRot[i0], curveRot[i1]) * Mth.DEG_TO_RAD);
            }
        }
    }

    /** 每侧 46 个 trackMov / trackRot 骨骼引用（按模型编号），按模型实例缓存 */
    private static final class TrackBones {

        private static VehicleModelInstance cachedInstance;
        private static TrackBones cached;

        final BoneState[] leftMov = new BoneState[HYR0TrackCurves.LINKS_PER_SIDE];
        final BoneState[] rightMov = new BoneState[HYR0TrackCurves.LINKS_PER_SIDE];
        final BoneState[] leftRot = new BoneState[HYR0TrackCurves.LINKS_PER_SIDE];
        final BoneState[] rightRot = new BoneState[HYR0TrackCurves.LINKS_PER_SIDE];
        int found;

        static TrackBones get(VehicleModelInstance instance) {
            if (instance == cachedInstance && cached != null) {
                return cached;
            }
            TrackBones bones = new TrackBones();
            for (int i = 0; i < HYR0TrackCurves.LINKS_PER_SIDE; i++) {
                bones.leftMov[i] = instance.getBone("trackMovL" + i);
                bones.rightMov[i] = instance.getBone("trackMovR" + i);
                bones.leftRot[i] = instance.getBone("trackRotL" + i);
                bones.rightRot[i] = instance.getBone("trackRotR" + i);
                if (bones.leftMov[i] != null && bones.rightMov[i] != null) {
                    bones.found++;
                }
            }
            if (bones.found == 0) {
                return null;
            }
            cachedInstance = instance;
            cached = bones;
            return bones;
        }

        /** 变树时隐藏、变回来时恢复全部履带块（Mov 与 Rot 都要设，链节几何在 Rot 上） */
        void setVisible(boolean visible) {
            for (BoneState[] group : new BoneState[][]{leftMov, rightMov, leftRot, rightRot}) {
                for (BoneState bone : group) {
                    if (bone != null) {
                        bone.visible = visible;
                    }
                }
            }
        }
    }
}
