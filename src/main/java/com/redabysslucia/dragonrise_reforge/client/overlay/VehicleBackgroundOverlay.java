package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.client.RenderHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VehicleBackgroundOverlay implements LayeredDraw.Layer {
    public static final String ID = Dragonrise_reforge.MODID + "_vehicle_background";

    private float scopeScale = 1f;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;
        if (player.isSpectator()) return;

        if (player.getVehicle() instanceof IVehicleBackground vehicle && vehicle.shouldRenderBackground()) {
            var texture = vehicle.getBackgroundTexture();
            if (texture == null) return;

            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
            RenderSystem.setShaderColor(1, 1, 1, 1);

            scopeScale = (float) Mth.lerp(0.5f * deltaTracker.getRealtimeDeltaTicks(), scopeScale, 1.35f);
            float f = Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
            float f1 = Math.min((float) guiGraphics.guiWidth() / f, (float) guiGraphics.guiHeight() / f) * scopeScale;
            float i = Mth.floor(f * f1);
            float j = Mth.floor(f * f1);
            float k = (guiGraphics.guiWidth() - i) / 2f;
            float l = (guiGraphics.guiHeight() - j) / 2f;
            float w = i * 21 / 9;
            RenderHelper.preciseBlit(guiGraphics, texture, k - (2 * w / 7), l, 0f, 0f, w, j, w, j);

            poseStack.popPose();
        } else {
            scopeScale = 1f;
        }
    }
}
