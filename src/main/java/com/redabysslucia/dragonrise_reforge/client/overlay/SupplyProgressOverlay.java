package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.AmmoSupplyStationEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.overlay.ForgeGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class SupplyProgressOverlay implements LayeredDraw.Layer {
    public static final String ID = Dragonrise_reforge.MODID + "_supply_progress";

    private static final float OUTER_RADIUS = 12f;
    private static final float INNER_RADIUS = 10f;
    private static final int RING_SEGMENTS = 64;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        float progress = getNearbySupplyProgress(mc);
        if (progress <= 0f) return;

        float centerX = guiGraphics.guiWidth() / 2f;
        float centerY = guiGraphics.guiHeight() / 2f;

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
        // TODO: Reimplement using 1.21.1 BufferBuilder/Tesselator API
        // The rendering API changed significantly in Minecraft 1.21.1.
        // BufferBuilder now takes (ByteBufferBuilder, Mode, VertexFormat)
        // Tesselator.getBuilder() is no longer available.
        // buffer.vertex(x, y, z) now uses addVertex(x, y, z)
        // tesselator.end() was replaced with BufferUploader.drawWithShader()
    }

    private void drawProgressArc(float cx, float cy, float progress) {
        // TODO: Reimplement using 1.21.1 BufferBuilder/Tesselator API
    }
}
