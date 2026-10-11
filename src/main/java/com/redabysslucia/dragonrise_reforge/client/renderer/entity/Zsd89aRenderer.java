package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.zsd89a.Zsd89aTrackCurves;
import com.redabysslucia.dragonrise_reforge.entities.Zsd89aEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * ZSD-89A 履带式装甲输送车 履带：每侧一条环，链节编号 L=1..56、R=0..57（与 ZTQ15 同构）。
 * <p>
 * 卓越前线的内置驱动用 {@code t = wrap(leftTrack + getTrackDistance() * index)}，假定链节从 0 连续编号、
 * 且 t 的循环长度等于 {@code getTrackAnimationLength()}；本模型编号从 1 起（右侧还有 0 号）、曲线以像素为 t 单位，
 * 所以这里在 {@code super} 之后按自己的曲线重新摆位。曲线见 {@link Zsd89aTrackCurves}（两种构型共用）。
 * 相位取车轮转角换算成像素（{@code 1.5 × wheelRot × 负重轮半径}），履带与车轮始终同步。
 */
public class Zsd89aRenderer extends GeoVehicleRenderer<Zsd89aEntity> {

    /** 履带环行进方向（与 HYR0 / ZTQ15 一致取负：曲线 t 增大方向与车轮前进转向相反） */
    private static final float TRACK_DIRECTION = -1f;
    /** 链节贴向切线的自转符号 */
    private static final float LINK_ROT_SIGN = 1f;
    /** 与 GeoVehicleRenderer 里车轮骨骼的旋转倍率一致 */
    private static final float WHEEL_SPIN_SCALE = 1.5f;
    /** 负重轮半径（模型单位，直径 10.96px） */
    private static final float ROAD_WHEEL_RADIUS = 10.96f / 2f;

    public Zsd89aRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(Zsd89aEntity entity, VehicleModelInstance instance, PoseStack poseStack,
                                        float entityYaw, float partialTicks) {
        // 车轮旋转 / 车体摇晃 / 炮塔交给 super；它按内置单链摆好的履带位置随后被覆盖
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        TrackBones bones = TrackBones.get(instance);
        if (bones == null) {
            return;     // LOD 模型或骨骼名不匹配
        }

        float leftPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getLeftWheelRot() * ROAD_WHEEL_RADIUS;
        float rightPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getRightWheelRot() * ROAD_WHEEL_RADIUS;

        drive(bones.leftMov, bones.leftRot, Zsd89aTrackCurves.LEFT_LINK_COUNT, leftPhase);
        drive(bones.rightMov, bones.rightRot, Zsd89aTrackCurves.RIGHT_LINK_COUNT, rightPhase);
    }

    /** 按曲线摆放一侧全部链节：环内等距（节距 = 环长 / 节数），相位以像素为单位循环 */
    private void drive(BoneState[] movBones, BoneState[] rotBones, int count, float phase) {
        float loopLength = Zsd89aTrackCurves.LOOP_LENGTH;
        float pitch = loopLength / count;

        for (int k = 0; k < count; k++) {
            if (k >= movBones.length || movBones[k] == null) {
                continue;
            }

            float t = phase + pitch * k;
            t = ((t % loopLength) + loopLength) % loopLength;
            float u = t / loopLength * (Zsd89aTrackCurves.SAMPLES - 1);
            int i0 = (int) u;
            int i1 = Math.min(i0 + 1, Zsd89aTrackCurves.SAMPLES - 1);
            float f = u - i0;

            BoneState mov = movBones[k];
            float pivotY = 0f;
            float pivotZ = 0f;
            var definition = mov.definition();
            if (definition != null) {
                pivotY = definition.pivotY() * 16f;
                pivotZ = definition.pivotZ() * 16f;
            }
            mov.y = pivotY + Mth.lerp(f, Zsd89aTrackCurves.Y[i0], Zsd89aTrackCurves.Y[i1]);
            mov.z = pivotZ + Mth.lerp(f, Zsd89aTrackCurves.Z[i0], Zsd89aTrackCurves.Z[i1]);

            if (rotBones != null && k < rotBones.length && rotBones[k] != null) {
                rotBones[k].rotation.rotationX(
                        LINK_ROT_SIGN * Mth.lerp(f, Zsd89aTrackCurves.ROT[i0], Zsd89aTrackCurves.ROT[i1]) * Mth.DEG_TO_RAD);
            }
        }
    }

    /** 两侧链节骨骼引用（按链节序号存放，模型编号 L=1.. / R=0..），按模型实例缓存 */
    private static final class TrackBones {

        private static VehicleModelInstance cachedInstance;
        private static TrackBones cached;

        final BoneState[] leftMov = new BoneState[Zsd89aTrackCurves.LEFT_LINK_COUNT];
        final BoneState[] rightMov = new BoneState[Zsd89aTrackCurves.RIGHT_LINK_COUNT];
        final BoneState[] leftRot = new BoneState[Zsd89aTrackCurves.LEFT_LINK_COUNT];
        final BoneState[] rightRot = new BoneState[Zsd89aTrackCurves.RIGHT_LINK_COUNT];
        int found;

        static TrackBones get(VehicleModelInstance instance) {
            if (instance == cachedInstance && cached != null) {
                return cached;
            }
            TrackBones bones = new TrackBones();
            for (int k = 0; k < bones.leftMov.length; k++) {
                int id = Zsd89aTrackCurves.LEFT_FIRST_INDEX + k;
                bones.leftMov[k] = instance.getBone("trackMovL" + id);
                bones.leftRot[k] = instance.getBone("trackRotL" + id);
                if (bones.leftMov[k] != null) {
                    bones.found++;
                }
            }
            for (int k = 0; k < bones.rightMov.length; k++) {
                int id = Zsd89aTrackCurves.RIGHT_FIRST_INDEX + k;
                bones.rightMov[k] = instance.getBone("trackMovR" + id);
                bones.rightRot[k] = instance.getBone("trackRotR" + id);
                if (bones.rightMov[k] != null) {
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
