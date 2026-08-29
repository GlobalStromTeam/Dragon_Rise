package com.redabysslucia.dragonrise_reforge.events;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.projectile.GuidedBombEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.SyncCameraVehicle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 投弹落点锁定框：
 * 炸弹投下后、落地爆炸前，屏幕上始终显示锁定框——
 * 炸弹在视角内时框套在炸弹屏幕投影位置；在视角外（含相机后方）时框固定夹紧在
 * 屏幕边缘并始终指向炸弹，任何视角都不消失。
 *
 * 归属判定：客户端炸弹实体的 owner(entityData) 因 superb BVR 同步机制不会到达客户端，
 * 由服务端在炸弹生成时通过 OwnBombMessage 通知 UUID（见 CommonEvent），
 * 客户端用 UUID 集合判断"是本玩家投下的炸弹"。
 */
@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvent {
    private static float lastSyncCameraYRot = 0;
    private static float lastSyncCameraXRot = 0;
    private static boolean isFirstSyncCameraFrame = true;

    /** 官方投弹落点环纹理（AircraftHud.BOMB_RING） */
    private static final ResourceLocation BOMB_RING =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/crosshair/rex_circle.png");

    /** 官方锁定框纹理（VehicleMainWeaponHudOverlay.FRAME_LOCK） */
    private static final ResourceLocation BOMB_LOCK_FRAME =
            ResourceLocation.fromNamespaceAndPath("superbwarfare", "textures/overlay/frame/frame_lock.png");

    /** 世界中的制导炸弹（不分玩家，渲染时按 UUID 归属过滤；由 join 事件 + 兜底扫描维护） */
    private static final List<GuidedBombEntity> bombsInWorld = new ArrayList<>();
    private static int scanCooldown = 0;
    private static final int SCAN_INTERVAL = 20;      // 每 20 tick 兜底扫描一次
    private static final double SCAN_RANGE = 512.0;   // 兜底扫描半径（格）

    /**
     * 本玩家投下的炸弹 UUID 集合（由服务端 OwnBombMessage 通知）。
     * 客户端炸弹实体的 owner(entityData) 因 superb BVR 同步机制不会到达客户端，
     * getOwner() 恒为 null，归属判断必须依赖服务端通知的 UUID。
     */
    private static final Set<UUID> ownBombUuids = new HashSet<>();

    /** 收到服务端通知：某颗炸弹是玩家投下的 */
    public static void onOwnBombMessage(UUID bombUuid) {
        ownBombUuids.add(bombUuid);
    }

    /** 调试日志（节流打印） */
    private static final Logger LOGGER = Dragonrise_reforge.LOGGER;
    private static int debugFrame = 0;

    // ==================== 炸弹列表维护 ====================

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof GuidedBombEntity bomb) {
            // 客户端分支：登记实体（此时 owner 尚未同步，不做归属过滤；归属由服务端 OwnBombMessage 通知）
            LOGGER.info("[BombHud] client join: bomb {} at {}", bomb.getUUID(), bomb.position());
            if (!bombsInWorld.contains(bomb)) {
                bombsInWorld.add(bomb);
            }
        }
    }

    /** 兜底扫描：join 事件不可靠时的补充；同时惰性清理已移除的炸弹 */
    private static void refreshBombList(Player player) {
        if (--scanCooldown > 0) return;
        scanCooldown = SCAN_INTERVAL;

        Vec3 c = player.position();
        List<GuidedBombEntity> found = player.level().getEntitiesOfClass(
                GuidedBombEntity.class,
                new AABB(c.x - SCAN_RANGE, c.y - SCAN_RANGE, c.z - SCAN_RANGE,
                        c.x + SCAN_RANGE, c.y + SCAN_RANGE, c.z + SCAN_RANGE),
                GuidedBombEntity::isAlive);
        if (!found.isEmpty()) {
            LOGGER.info("[BombHud] scan found {} bomb(s)", found.size());
            for (GuidedBombEntity b : found) {
                if (!bombsInWorld.contains(b)) {
                    bombsInWorld.add(b);
                }
            }
        }
        bombsInWorld.removeIf(b -> b.isRemoved() || b.level() != player.level());
    }

    /** 是否为本玩家投下且仍在飞行的炸弹（归属由服务端通知的 UUID 集合判断） */
    private static boolean isOwnBomb(GuidedBombEntity bomb, Player player) {
        boolean removed = bomb.isRemoved();
        boolean sameLevel = bomb.level() == player.level();
        boolean own = ownBombUuids.contains(bomb.getUUID());
        if (!own) {
            // 兜底：若 owner 同步正常也认（某些环境可能可用）
            own = bomb.getOwner() == player;
        }
        if (++debugFrame % 400 == 0) {
            LOGGER.info("[BombHud] isOwnBomb removed={} sameLevel={} own={} uuid={} knownUuids={}",
                    removed, sameLevel, own, bomb.getUUID(), ownBombUuids.size());
        }
        return !removed && sameLevel && own;
    }

    // ==================== 辅助：屏幕落点锁定框 ====================

    /**
     * 世界坐标 → GUI 屏幕坐标（自实现透视投影，不依赖 superb 从未赋值的
     * ClientEventHandler.modelViewMatrix/projectionMatrix）。
     * 返回 Vec3(x=屏幕x, y=屏幕y, z=NDC深度)；相机后方坐标已镜像翻转，深度标记为 2.0。
     */
    private static Vec3 projectWorldToScreen(Vec3 worldPos, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Vector3f look = camera.getLookVector();
        Vector3f up = camera.getUpVector();

        // superb 的 currentFov 只在缩放/吊舱分支写入,退出缩放后保持旧值(滞后),
        // 因此非缩放状态必须用配置 fov 换算,否则渲染位置会按缩放 fov 偏移
        float fov;
        if (ClientEventHandler.zoomVehicle) {
            fov = (float) ClientEventHandler.currentFov;
            if (fov <= 0.0f) {
                fov = (float) mc.options.fov().get();
            }
        } else {
            fov = (float) mc.options.fov().get();
        }
        double aspect = mc.getWindow().getScreenWidth() / (double) mc.getWindow().getScreenHeight();

        Matrix4f view = new Matrix4f().lookAt(
                new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z),
                new Vector3f((float) camPos.x + look.x(), (float) camPos.y + look.y(), (float) camPos.z + look.z()),
                up);
        Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(fov), (float) aspect, 0.05f, 20000.0f);

        Matrix4f combined = new Matrix4f(proj).mul(view);
        Vector4f clip = combined.transform(
                new Vector4f((float) worldPos.x, (float) worldPos.y, (float) worldPos.z, 1.0f));
        float w = clip.w;
        // 相机后方(w<0)时用 |w| 归一，屏幕坐标自动镜像翻转(指向正确方向)，深度标记 2.0
        float absW = Math.abs(w);
        if (absW < 1.0e-8f) {
            return null;
        }
        float ndcX = clip.x / absW;
        float ndcY = clip.y / absW;
        float depth = w <= 0.0f ? 2.0f : clip.z / w;
        return new Vec3(
                mc.getWindow().getGuiScaledWidth() * (0.5 + ndcX * 0.5),
                mc.getWindow().getGuiScaledHeight() * (0.5 - ndcY * 0.5),
                depth);
    }

    /** 投弹吊舱视角（UseNacelleCamera 缩放中）显示提前落点计算环 */
    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (!(player.getVehicle() instanceof VehicleEntity vehicle)) return;

        GunData data = vehicle.getGunData(player);
        if (data == null) return;
        if (!"@AirBomb".equals(data.get(GunProp.CROSSHAIR))) return;
        if (!ClientEventHandler.zoomVehicle) return; // 仅吊舱/缩放视角（UseNacelleCamera 武器缩放即吊舱视角）

        Vec3 bombO = ClientEventHandler.bombHitPosO;
        Vec3 bombN = ClientEventHandler.bombHitPos;
        if (bombO.equals(Vec3.ZERO) && bombN.equals(Vec3.ZERO)) return;

        float pt = event.getPartialTick();
        Vec3 pos = new Vec3(
                Mth.lerp(pt, bombO.x, bombN.x),
                Mth.lerp(pt, bombO.y, bombN.y),
                Mth.lerp(pt, bombO.z, bombN.z));
        Vec3 p = projectWorldToScreen(pos, pt);
        if (p == null) return; // 投影无效
        if (p.z >= 1.0) return; // 在相机后方

        float x = (float) p.x;
        float y = (float) p.y;
        if (x < -40 || x > mc.getWindow().getGuiScaledWidth() + 40
                || y < -40 || y > mc.getWindow().getGuiScaledHeight() + 40) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderHelper.preciseBlit(event.getGuiGraphics(), BOMB_RING, x - 12f, y - 12f, 0f, 0f, 24f, 24f, 24f, 24f);
    }

    /**
     * 投弹后的炸弹落点锁定框：锁定框标记给炸弹设定的制导目标落点
     * （bomb.getTargetPos()，TARGET_POS 为 entityData 已同步——激光模式下为吊舱当前
     * 瞄准点并随视线移动，卫星模式下为锁定坐标）——
     * 目标点在视角内时框套在其屏幕投影位置；在视角外（含相机后方）时框固定夹紧在
     * 屏幕边缘并始终指向目标点。从投弹到爆炸全程显示。
     */
    @SubscribeEvent
    public static void onRenderBombLockFrame(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.level() == null) return;
        if (!(player.getVehicle() instanceof VehicleEntity)) return;

        refreshBombList(player);
        if (bombsInWorld.isEmpty()) return;

        float pt = event.getPartialTick();
        double cx = mc.getWindow().getGuiScaledWidth() / 2.0;
        double cy = mc.getWindow().getGuiScaledHeight() / 2.0;
        final double margin = 30.0;

        for (GuidedBombEntity bomb : bombsInWorld) {
            if (!isOwnBomb(bomb, player)) continue;
            // 未锁定的炸弹（激光随动/无锁定目标）不渲染锁定框，避免无意义开销
            if (!bomb.isLocked()) continue;

            Vec3 target = bomb.getTargetPos();
            if (target == null) continue;
            Vec3 p = projectWorldToScreen(target, pt);
            if (p == null) continue;

            double dx = p.x - cx;
            double dy = p.y - cy;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len < 1.0e-6) continue;

            // 夹紧到屏幕边缘（留 30px 边距）
            double scale = Math.min((cx - margin) / Math.abs(dx), (cy - margin) / Math.abs(dy));
            if (scale > 1.0) scale = 1.0;
            float fx = (float) (cx + dx * scale);
            float fy = (float) (cy + dy * scale);

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderHelper.blit(event.getGuiGraphics().pose(), BOMB_LOCK_FRAME,
                    fx - 12f, fy - 12f, 0f, 0f, 24f, 24f, 24f, 24f, 1f);
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        var mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.getVehicle() instanceof SyncCameraVehicle vehicle) {
            float partialTicks = event.renderTickTime;

            float prevYRot = vehicle.yRotO;
            float currYRot = vehicle.getYRot();
            float prevXRot = vehicle.xRotO;
            float currXRot = vehicle.getXRot();

            float interpolatedYRot = net.minecraft.util.Mth.lerp(partialTicks, prevYRot, currYRot);
            float interpolatedXRot = net.minecraft.util.Mth.lerp(partialTicks, prevXRot, currXRot);

            if (isFirstSyncCameraFrame) {
                lastSyncCameraYRot = interpolatedYRot;
                lastSyncCameraXRot = interpolatedXRot;
                isFirstSyncCameraFrame = false;
                return;
            }

            float yRotDelta = interpolatedYRot - lastSyncCameraYRot;
            float xRotDelta = interpolatedXRot - lastSyncCameraXRot;

            // 处理旋转环绕问题 (-180到180度)
            yRotDelta = net.minecraft.util.Mth.wrapDegrees(yRotDelta);

            player.setYHeadRot(player.getYHeadRot() + yRotDelta);
            player.setYRot(player.getYRot() + yRotDelta);
            player.setXRot(player.getXRot() + xRotDelta);

            lastSyncCameraYRot = interpolatedYRot;
            lastSyncCameraXRot = interpolatedXRot;
        } else {
            isFirstSyncCameraFrame = true;
        }
    }
}
