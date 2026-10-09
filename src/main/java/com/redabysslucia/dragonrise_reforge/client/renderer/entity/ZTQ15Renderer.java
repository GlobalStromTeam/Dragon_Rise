package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModelInstance;
import com.atsuishio.superbwarfare.client.renderer.entity.GeoVehicleRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BoneState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.ztq15.ZTQ15TrackCurves;
import com.redabysslucia.dragonrise_reforge.entities.ZTQ15Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * ZTQ15 履带：每侧一条环，但卓越前线的内置驱动用的是
 * {@code t = wrap(leftTrack + getTrackDistance() * index)} —— 它假定链节沿环均匀分布、t 的循环长度等于
 * {@code getTrackAnimationLength()}；本模型的链节编号从 1 开始（右侧还多一个 0 号）、且我们的曲线以像素为 t 单位，
 * 两者对不上，所以这里在 {@code super} 之后按自己的曲线重新摆位。
 * <p>
 * 曲线见 {@link ZTQ15TrackCurves}（由模型轮组几何生成）：前导引轮 Ø7.05px @z=-53.10、
 * 负重轮 ×6 Ø8.27px @y=6.53、后主动轮 Ø8.60px @z=46.07，环周长 230.8px、节距 4.12px。
 * 相位取车轮转角换算成像素（{@code 1.5 × wheelRot × 负重轮半径}），履带与车轮始终同步。
 */
public class ZTQ15Renderer extends GeoVehicleRenderer<ZTQ15Entity> {

    /** 履带环行进方向：与 HYR0 相同取负（曲线 t 增大方向与车轮前进转向相反） */
    private static final float TRACK_DIRECTION = -1f;
    /** 链节贴向切线的自转符号：与 HYR0 相同约定 */
    private static final float LINK_ROT_SIGN = 1f;
    /** 与 GeoVehicleRenderer 里车轮骨骼的旋转倍率一致 */
    private static final float WHEEL_SPIN_SCALE = 1.5f;
    /** 负重轮半径（模型单位，直径 8.27px）——履带线速度 = 车轮角速度 × 此半径 */
    private static final float ROAD_WHEEL_RADIUS = 8.27f / 2f;

    public ZTQ15Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public void transformCustomModelPart(ZTQ15Entity entity, VehicleModelInstance instance, PoseStack poseStack,
                                        float entityYaw, float partialTicks) {
        // 车轮旋转 / 车体摇晃 / 炮塔交给 super；它按内置单链摆好的履带位置随后被覆盖
        super.transformCustomModelPart(entity, instance, poseStack, entityYaw, partialTicks);

        TrackBones bones = TrackBones.get(instance);
        if (bones == null) {
            return;     // LOD 模型或骨骼名不匹配
        }

        float leftPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getLeftWheelRot() * ROAD_WHEEL_RADIUS;
        float rightPhase = TRACK_DIRECTION * WHEEL_SPIN_SCALE * getRightWheelRot() * ROAD_WHEEL_RADIUS;

        drive(bones.leftMov, bones.leftRot, ZTQ15TrackCurves.LEFT_LINK_COUNT, leftPhase);
        drive(bones.rightMov, bones.rightRot, ZTQ15TrackCurves.RIGHT_LINK_COUNT, rightPhase);
    }

    /** 按曲线摆放一侧全部链节：环内等距（节距 = 环长 / 节数），相位以像素为单位循环 */
    private void drive(BoneState[] movBones, BoneState[] rotBones, int count, float phase) {
        float loopLength = ZTQ15TrackCurves.LOOP_LENGTH;
        float pitch = loopLength / count;

        for (int k = 0; k < count; k++) {
            if (k >= movBones.length || movBones[k] == null) {
                continue;
            }

            float t = phase + pitch * k;
            t = ((t % loopLength) + loopLength) % loopLength;
            float u = t / loopLength * (ZTQ15TrackCurves.SAMPLES - 1);
            int i0 = (int) u;
            int i1 = Math.min(i0 + 1, ZTQ15TrackCurves.SAMPLES - 1);
            float f = u - i0;

            BoneState mov = movBones[k];
            float pivotY = 0f;
            float pivotZ = 0f;
            var definition = mov.definition();
            if (definition != null) {
                pivotY = definition.pivotY() * 16f;
                pivotZ = definition.pivotZ() * 16f;
            }
            mov.y = pivotY + Mth.lerp(f, ZTQ15TrackCurves.Y[i0], ZTQ15TrackCurves.Y[i1]);
            mov.z = pivotZ + Mth.lerp(f, ZTQ15TrackCurves.Z[i0], ZTQ15TrackCurves.Z[i1]);

            if (rotBones != null && k < rotBones.length && rotBones[k] != null) {
                rotBones[k].rotation.rotationX(
                        LINK_ROT_SIGN * Mth.lerp(f, ZTQ15TrackCurves.ROT[i0], ZTQ15TrackCurves.ROT[i1]) * Mth.DEG_TO_RAD);
            }
        }
    }

    /** 两侧链节骨骼引用（按链节序号存放，模型编号 L=1..、R=0..），按模型实例缓存 */
    private static final class TrackBones {

        private static VehicleModelInstance cachedInstance;
        private static TrackBones cached;

        final BoneState[] leftMov = new BoneState[ZTQ15TrackCurves.LEFT_LINK_COUNT];
        final BoneState[] rightMov = new BoneState[ZTQ15TrackCurves.RIGHT_LINK_COUNT];
        final BoneState[] leftRot = new BoneState[ZTQ15TrackCurves.LEFT_LINK_COUNT];
        final BoneState[] rightRot = new BoneState[ZTQ15TrackCurves.RIGHT_LINK_COUNT];
        int found;

        static TrackBones get(VehicleModelInstance instance) {
            if (instance == cachedInstance && cached != null) {
                return cached;
            }
            TrackBones bones = new TrackBones();
            for (int k = 0; k < bones.leftMov.length; k++) {
                int id = ZTQ15TrackCurves.LEFT_FIRST_INDEX + k;
                bones.leftMov[k] = instance.getBone("trackMovL" + id);
                bones.leftRot[k] = instance.getBone("trackRotL" + id);
                if (bones.leftMov[k] != null) {
                    bones.found++;
                }
            }
            for (int k = 0; k < bones.rightMov.length; k++) {
                int id = ZTQ15TrackCurves.RIGHT_FIRST_INDEX + k;
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
