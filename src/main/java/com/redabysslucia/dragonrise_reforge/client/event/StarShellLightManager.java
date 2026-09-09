package com.redabysslucia.dragonrise_reforge.client.event;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.special.StarShellEntity;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import toni.sodiumdynamiclights.SodiumDynamicLights;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 照明弹补光「刷新调度」（客户端）。
 *
 * 光照数据本身没问题（光已烘焙进 72 格范围）；此前所有重建尝试（vanilla setSectionDirty /
 * setBlocksDirty / sodium scheduleRebuildForChunk…）都无效，且 SodiumWorldRenderer.reload()
 * 在 ClientTickEvent 里调用会崩（checkDeviceActive：需要 GL 上下文）。结论：embeddium 的
 * 重建调度必须在【渲染帧（RenderTickEvent，GL 活跃）】内发起 —— reforged 自身的有效调用
 * （WorldRendererMixin.beforeRender → updateAll）正是渲染期。
 *
 * 因此全部逻辑移到 RenderTickEvent：事件（悬停开始 / 消失）触发把光区 section 入队，
 * 每个渲染 tick 限量把队列交给 SodiumWorldRenderer.scheduleRebuildForChunk（embeddium
 * 原生调度，渲染期执行必然生效）。
 */
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID, value = Dist.CLIENT)
public class StarShellLightManager {

    /** 光区 XZ 半径（chunk/section）：±4 = ±64 格 */
    private static final int XZ_RADIUS = 4;
    /** 每个渲染 tick 最多执行的调度数（调大 → 铺开更快；embeddium 编译仍按自身预算限速） */
    private static final int PER_TICK = 160;

    private static final Map<Integer, Tracked> TRACKED = new HashMap<>();
    private static final Deque<Long> PENDING = new ArrayDeque<>();

    private static final class Tracked {
        int sx, sy, sz;
        boolean hovering;
        final StarShellEntity entity;

        Tracked(StarShellEntity entity) {
            this.entity = entity;
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.levelRenderer == null) return;

        // ---- 扫描悬停状态变化 ----
        Set<Integer> seen = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof StarShellEntity shell)) continue;
            int id = shell.getId();
            seen.add(id);
            int sx = SectionPos.blockToSectionCoord(shell.getX());
            int sy = SectionPos.blockToSectionCoord(shell.getY());
            int sz = SectionPos.blockToSectionCoord(shell.getZ());
            boolean hovering = shell.isHovering();
            Tracked t = TRACKED.get(id);
            if (t == null) {
                t = new Tracked(shell);
                TRACKED.put(id, t);
                t.sx = sx;
                t.sy = sy;
                t.sz = sz;
                t.hovering = hovering;
                if (hovering) requestRefresh(sx, sy, sz);
            } else if (!t.hovering && hovering) {
                requestRefresh(sx, sy, sz);
                t.hovering = true;
            } else if (t.sx != sx || t.sy != sy || t.sz != sz) {
                t.sx = sx;
                t.sy = sy;
                t.sz = sz;
            }
        }

        // ---- 消失：移除光源 + 擦光重建 ----
        var it = TRACKED.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            if (!seen.contains(e.getKey())) {
                Tracked t = e.getValue();
                if (t.hovering) {
                    SodiumDynamicLights dl = SodiumDynamicLights.get();
                    if (dl != null && t.entity instanceof toni.sodiumdynamiclights.DynamicLightSource dls) {
                        dl.removeLightSource(dls);
                    }
                    requestRefresh(t.sx, t.sy, t.sz);
                }
                it.remove();
            }
        }

        // ---- 渲染期执行调度（GL 活跃，embeddium 才消费）----
        if (PENDING.isEmpty()) return;
        SodiumWorldRenderer swr = SodiumWorldRenderer.instanceNullable();
        if (swr == null) return;
        int n = Math.min(PER_TICK, PENDING.size());
        for (int i = 0; i < n; i++) {
            long sec = PENDING.poll();
            swr.scheduleRebuildForChunk(BlockPos.getX(sec), BlockPos.getY(sec), BlockPos.getZ(sec), true);
        }
    }

    /** 请求一次强制刷新（悬停开始 / 弹消失触发） */
    private static void requestRefresh(int sx, int sy, int sz) {
        enqueue(sx, sy, sz);
    }

    /**
     * 以 (sx,sy,sz)（section）为中心入队光区重建。
     * y 层覆盖弹所在深度：弹下方 5 层到弹上方 1 层（±80 格 ≥ LIGHT_RADIUS 72）。
     * 之前 bug：写成 [0, max(0,sy+1)] —— 弹/玩家在地下（sy≈-4）时永远只重建 y=0 地表层
     * （距弹远超 72 格无光），真正该亮的洞壁 section 从未重建 → 全黑、只有重进世界才亮。
     */
    private static void enqueue(int sx, int sy, int sz) {
        if (PENDING.size() > 20000) PENDING.clear();
        int yMin = sy - 5;
        int yMax = sy + 1;
        for (int ring = 0; ring <= XZ_RADIUS; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    for (int y = yMin; y <= yMax; y++) {
                        PENDING.add(BlockPos.asLong(sx + dx, y, sz + dz));
                    }
                }
            }
        }
    }
}
