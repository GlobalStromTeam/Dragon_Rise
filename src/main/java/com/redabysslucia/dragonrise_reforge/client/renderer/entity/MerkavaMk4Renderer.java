package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.merkava.MerkavaMk4TrackCurves;
import com.redabysslucia.dragonrise_reforge.entities.MerkavaMk4Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * Merkava Mk.4 履带。
 * <p>
 * 模型本身没有可用的履带环：原模型的链节是"绕远处枢轴转 −42.5° + 骨骼再转回 +42.5°"的写法，
 * 每侧只有 32/34 节散落的垫块，且没有任何 {@code trackMov*} / {@code trackRot*} 骨骼，
 * 所以卓越前线内置的单链驱动完全不会动。这里把模型改成规范结构（见 {@code build/merkava_tracks.py}）：
 * <ul>
 *   <li>轮组按几何重新命名：左侧 {@code wheelL1..8}（x&gt;0）、右侧 {@code wheelR1..8}（x&lt;0），
 *       1 号前导引轮、8 号后主动轮；</li>
 *   <li>链节统一成 {@code trackMov{LR}{i}}（空父骨负责平移）+ {@code trackRot{LR}{i}}（携带垫块几何，负责自转），
 *       每侧补齐到 {@link MerkavaMk4TrackCurves#LINKS} 节；</li>
 *   <li>环线由轮盘几何算出（{@link MerkavaMk4TrackCurves}），左右共用。</li>
 * </ul>
 * 相位取车轮转角换算成像素（{@code 1.5 * wheelRot * 主动轮半径}），左右独立处理差速。
 */
public class MerkavaMk4Renderer extends GeoVehicleRenderer<MerkavaMk4Entity> {

    /** 履带环的行进方向：+1 = 按曲线 t 增大方向跑（顶边朝 -z），-1 = 反向（与 HYR0 一致，实机验证过） */
    private static final float TRACK_DIRECTION = -1f;
    /** 与 GeoVehicleRenderer 里车轮骨骼的旋转倍率一致（bone.rotation.rotationX(1.5f * wheelRot)） */
    private static final float WHEEL_SPIN_SCALE = 1.5f;
    /** 后主动轮半径（模型像素），履带线速度 = 车轮角速度 × 此半径 */
    private static final float DRIVE_WHEEL_RADIUS = 9.64135f / 2f;

    public MerkavaMk4Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(MerkavaMk4Entity entity, VehicleModelInstance instance, PoseStack poseStack,
                                        float entityYaw, float partialTicks) {
        // 车轮旋转、车体摇晃、炮塔等都交给 super；它顺手按单链放好的履带位置随后会被覆盖
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        TrackBones bones = TrackBones.get(instance);
        if (bones == null) {
            return;     // LOD 模型或骨骼名不匹配时不处理
        }

        float leftPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getLeftWheelRot() * DRIVE_WHEEL_RADIUS;
        float rightPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getRightWheelRot() * DRIVE_WHEEL_RADIUS;

        driveLoop(bones.leftMov, bones.leftRot, leftPhase);
        driveLoop(bones.rightMov, bones.rightRot, rightPhase);
    }

    /** 把每侧链节按曲线等距排布；相位以像素为单位在环上循环 */
    private static void driveLoop(BoneState[] movBones, BoneState[] rotBones, float phase) {
        int count = Math.min(movBones.length, MerkavaMk4TrackCurves.LINKS);
        if (count <= 0) {
            return;
        }
        float pitch = MerkavaMk4TrackCurves.LOOP_LENGTH / count;

        for (int k = 0; k < count; k++) {
            BoneState mov = movBones[k];
            if (mov == null) {
                continue;
            }

            // 环上位置 → 曲线采样下标（曲线按弧长等分，所以是线性的）
            float t = phase + pitch * k;
            t = ((t % MerkavaMk4TrackCurves.LOOP_LENGTH) + MerkavaMk4TrackCurves.LOOP_LENGTH)
                    % MerkavaMk4TrackCurves.LOOP_LENGTH;
            float u = t / MerkavaMk4TrackCurves.LOOP_LENGTH * (MerkavaMk4TrackCurves.SAMPLES - 1);
            int i0 = (int) u;
            int i1 = Math.min(i0 + 1, MerkavaMk4TrackCurves.SAMPLES - 1);
            float f = u - i0;

            // 曲线是模型绝对坐标；BoneState 的位置则相对 bind pose。
            // 保留 bind 位移并减掉原枢轴，兼容父骨 q2/q3 被保留或静态折叠的模型。
            var definition = mov.definition();
            mov.y = definition.bindY() - definition.pivotY() * 16f
                    + Mth.lerp(f, MerkavaMk4TrackCurves.Y[i0], MerkavaMk4TrackCurves.Y[i1]);
            mov.z = definition.bindZ() - definition.pivotZ() * 16f
                    + Mth.lerp(f, MerkavaMk4TrackCurves.Z[i0], MerkavaMk4TrackCurves.Z[i1]);

            if (k < rotBones.length && rotBones[k] != null) {
                rotBones[k].rotation.rotationX(
                        Mth.rotLerp(f, MerkavaMk4TrackCurves.ROT[i0], MerkavaMk4TrackCurves.ROT[i1]) * Mth.DEG_TO_RAD);
            }
        }
    }

    /** trackMov / trackRot 骨骼引用（按模型编号），按模型实例缓存 */
    private static final class TrackBones {

        private static VehicleModelInstance cachedInstance;
        private static TrackBones cached;

        final BoneState[] leftMov = new BoneState[MerkavaMk4TrackCurves.LINKS];
        final BoneState[] rightMov = new BoneState[MerkavaMk4TrackCurves.LINKS];
        final BoneState[] leftRot = new BoneState[MerkavaMk4TrackCurves.LINKS];
        final BoneState[] rightRot = new BoneState[MerkavaMk4TrackCurves.LINKS];
        int found;

        static TrackBones get(VehicleModelInstance instance) {
            if (instance == cachedInstance && cached != null) {
                return cached;
            }
            TrackBones bones = new TrackBones();
            for (int i = 0; i < MerkavaMk4TrackCurves.LINKS; i++) {
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
    }
}
