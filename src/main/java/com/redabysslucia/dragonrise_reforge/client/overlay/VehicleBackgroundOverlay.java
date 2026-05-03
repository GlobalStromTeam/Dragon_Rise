package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class VehicleBackgroundOverlay implements IGuiOverlay {
    public static final String ID = Dragonrise_reforge.MODID + "_vehicle_background";

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        Player player = mc.player;

        if (player == null) return;
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;
        if (player.isSpectator()) return;

        if (player.getVehicle() instanceof IVehicleBackground vehicle && vehicle.shouldRenderBackground()) {
            var texture = vehicle.getBackgroundTexture();
            if (texture == null) return;

            float alpha = vehicle.getBackgroundAlpha();

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
            RenderSystem.setShaderColor(1, 1, 1, alpha);

            guiGraphics.blit(texture, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}
