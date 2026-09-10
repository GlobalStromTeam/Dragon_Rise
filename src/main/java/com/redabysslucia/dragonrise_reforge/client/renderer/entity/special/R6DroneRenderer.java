package com.redabysslucia.dragonrise_reforge.client.renderer.entity.special;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.BedrockModelRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import com.redabysslucia.dragonrise_reforge.resource.model.EntityModelReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 无人车渲染器（侦察/攻击变种共用）：SBM 加载 models/bedrock/entity/{r6_drone|attack_drone}.geo.json。
 * 车体朝向：控制端直接用玩家视角（即时跟随、与相机一致），其他客户端用同步角度。
 * 位置：LevelRenderer 把车体放在 lerp(xOld, x) 线性插值位置，这里修正到实体的
 * Catmull-Rom 样条位置（getRenderPosition），与相机 mixin 使用同一平滑位置，杜绝错位。
 * <p>
 * 本类是纯客户端类（client-only）：控制端"本机玩家是否在遥控本车"的判断、
 * 平滑位置失效（resetSmoothState）都集中在这里 —— 原实现在 R6DroneEntity 中引用
 * Minecraft/LocalPlayer，会导致专用服务器（DEDICATED_SERVER）加载 common 实体类失败。
 */
public class R6DroneRenderer extends EntityRenderer<R6DroneEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation("dragonrise_reforge", "models/bedrock/entity/r6_drone.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("dragonrise_reforge", "textures/entity/r6_drone.png");
    private static final ResourceLocation ATTACK_MODEL =
            new ResourceLocation("dragonrise_reforge", "models/bedrock/entity/attack_drone.geo.json");
    private static final ResourceLocation ATTACK_TEXTURE =
            new ResourceLocation("dragonrise_reforge", "textures/entity/attack_drone.png");

    public R6DroneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static boolean isAttack(R6DroneEntity entity) {
        return entity instanceof com.redabysslucia.dragonrise_reforge.entities.special.AttackDroneEntity;
    }

    /** 本机玩家是否正在遥控该无人车（渲染线程，每帧可安全访问 Minecraft） */
    private static boolean isLocallyControlled(R6DroneEntity entity) {
        LocalPlayer local = Minecraft.getInstance().player;
        if (local == null) return false;
        return entity.getController() == local && entity.isMonitorControlling(local);
    }

    @Override
    public ResourceLocation getTextureLocation(R6DroneEntity entity) {
        return isAttack(entity) ? ATTACK_TEXTURE : TEXTURE;
    }

    @Override
    public void render(R6DroneEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        boolean attack = isAttack(entity);
        ResourceLocation modelLoc = attack ? ATTACK_MODEL : MODEL;
        ResourceLocation texture = attack ? ATTACK_TEXTURE : TEXTURE;
        var model = EntityModelReloadListener.INSTANCE.getModel(modelLoc);
        if (model == null) return;

        // 本机玩家未遥控本车：平滑位置失效（advanceSmoothPosition 只由遥控相机推进，
        // 退出遥控后其值滞留旧位置，须让渲染回退到样条位置，避免模型滞留半空）。
        // 遥控中由 R6DroneCameraMixin 每帧 advanceSmoothPosition 推进，这里不重置。
        boolean localControlling = isLocallyControlled(entity);
        if (!localControlling) {
            entity.resetSmoothState();
        }

        poseStack.pushPose();

        // 位置修正：LevelRenderer 已把 poseStack 平移到线性插值位置 lerp(xOld, x)，
        // 平移差值到相机同一平滑位置（getSmoothPositionOrNull，由 Camera.setup 每帧推进），
        // 使车体与相机完全一致；未初始化时退回样条位置。
        Vec3 smooth = entity.getSmoothPositionOrNull();
        Vec3 spline = smooth != null ? smooth : entity.getRenderPosition(partialTick);
        double lx = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double ly = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double lz = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        poseStack.translate(spline.x - lx, spline.y - ly, spline.z - lz);

        // 车体朝向：本机控制者直接用玩家视角（即时），否则同步角度
        float bodyYaw;
        float bodyXRot;
        if (localControlling) {
            LocalPlayer local = Minecraft.getInstance().player;
            bodyYaw = local.getYRot();
            bodyXRot = local.getXRot();
        } else {
            bodyYaw = entity.getRenderYaw(partialTick);
            bodyXRot = entity.getRenderPitch(partialTick);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-bodyXRot));

        // 模型略微下沉使其贴地（实体原点在脚底）
        poseStack.translate(0.0, -0.06, 0.0);

        model.renderToBuffer(
                poseStack,
                buffer,
                RenderType.entityCutout(texture),
                BedrockModelRenderTypes.polyMeshCutout(texture),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
