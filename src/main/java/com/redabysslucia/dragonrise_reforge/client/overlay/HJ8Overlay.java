package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.overlay.ForgeGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

import static com.atsuishio.superbwarfare.client.RenderHelper.preciseBlit;

@OnlyIn(Dist.CLIENT)
public class HJ8Overlay implements LayeredDraw.Layer {
    public static final String ID = Mod.MODID + "_hj8";

    private static final ResourceLocation SPYGLASS = Mod.loc("textures/overlay/spyglass/spyglass.png");

    private static float scopeScale = 1;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();
        Player player = mc.player;

        if (player == null) return;

        if (player.getVehicle() instanceof HJ8Entity && (ClientEventHandler.zoomVehicle || mc.options.getCameraType() == CameraType.FIRST_PERSON)) {
            poseStack.pushPose();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            float deltaFrame = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            scopeScale = (float) Mth.lerp(0.5F * deltaFrame, scopeScale, 1.35F + (0.2f * ClientEventHandler.firePosTimer));
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            float f = (float) Math.min(screenWidth, screenHeight);
            float f1 = Math.min((float) screenWidth / f, (float) screenHeight / f) * scopeScale;
            float i = Mth.floor(f * f1);
            float j = Mth.floor(f * f1);
            float k = ((screenWidth - i) / 2);
            float l = ((screenHeight - j) / 2);
            float w = i * 21 / 9;
            preciseBlit(guiGraphics, SPYGLASS, k - (2 * w / 7), l, 0, 0, w, j, w, j);
            poseStack.popPose();
        } else {
            scopeScale = 1;
        }
    }
}
