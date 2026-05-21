package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.AmmoSupplyStationEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class SupplyProgressOverlay implements IGuiOverlay {
    public static final String ID = Dragonrise_reforge.MODID + "_supply_progress";

    private static final float OUTER_RADIUS = 12f;
    private static final float INNER_RADIUS = 10f;
    private static final int RING_SEGMENTS = 64;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        if (mc.player == null || mc.level == null) return;

        float progress = getNearbySupplyProgress(mc);
        if (progress <= 0f) return;

        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        drawShadowRing(centerX, centerY);
        drawProgressArc(centerX, centerY, progress);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private float getNearbySupplyProgress(Minecraft mc) {
        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof GeoVehicleEntity)) {
            return 0f;
        }

        AABB searchBox = mc.player.getBoundingBox().inflate(64);
        for (Entity entity : mc.level.getEntities(mc.player, searchBox, e -> e instanceof AmmoSupplyStationEntity)) {
            AmmoSupplyStationEntity station = (AmmoSupplyStationEntity) entity;
            if (station.isCharging() && station.distanceToSqr(vehicle) <= station.getSupplyRange() * station.getSupplyRange()) {
                return station.getSupplyProgress();
            }
        }
        return 0f;
    }

    private void drawShadowRing(float cx, float cy) {
        float shadowOuter = OUTER_RADIUS + 1;
        float shadowInner = INNER_RADIUS - 1;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= RING_SEGMENTS; i++) {
            double angle = (i / (double) RING_SEGMENTS) * Math.PI * 2 - Math.PI / 2;
            float x = (float) Math.cos(angle);
            float y = (float) Math.sin(angle);
            buffer.vertex(cx + x * shadowOuter, cy + y * shadowOuter, 0).color(0, 0, 0, 60).endVertex();
            buffer.vertex(cx + x * shadowInner, cy + y * shadowInner, 0).color(0, 0, 0, 30).endVertex();
        }

        tesselator.end();
    }

    private void drawProgressArc(float cx, float cy, float progress) {
        float progressClamped = Math.min(1f, Math.max(0f, progress));
        int filledSegments = (int) (RING_SEGMENTS * progressClamped);

        float r = OUTER_RADIUS;
        float ir = INNER_RADIUS;
        float startAngle = (float) (-Math.PI / 2);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        if (filledSegments > 0) {
            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= filledSegments; i++) {
                double angle = startAngle + (i / (double) RING_SEGMENTS) * Math.PI * 2;
                float x = (float) Math.cos(angle);
                float y = (float) Math.sin(angle);
                buffer.vertex(cx + x * r, cy + y * r, 0).color(255, 255, 255, 220).endVertex();
                buffer.vertex(cx + x * ir, cy + y * ir, 0).color(255, 255, 255, 200).endVertex();
            }

            tesselator.end();
        }

        int remainingSegments = RING_SEGMENTS - filledSegments;
        if (remainingSegments > 0) {
            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = filledSegments; i <= RING_SEGMENTS; i++) {
                double angle = startAngle + (i / (double) RING_SEGMENTS) * Math.PI * 2;
                float x = (float) Math.cos(angle);
                float y = (float) Math.sin(angle);
                buffer.vertex(cx + x * r, cy + y * r, 0).color(255, 255, 255, 60).endVertex();
                buffer.vertex(cx + x * ir, cy + y * ir, 0).color(255, 255, 255, 40).endVertex();
            }

            tesselator.end();
        }
    }
}
