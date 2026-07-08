package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.weapon.LandVehicleHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LandVehicleHud.class, remap = false)
public class LandVehicleHudMixin {

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/atsuishio/superbwarfare/client/RenderHelper;preciseBlit(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;FFFFFFFFF)V"
        )
    )
    private void cancelScanlinePreciseBlit(
        GuiGraphics gui, ResourceLocation pAtlasLocation,
        float pX, float pY, float pBlitOffset,
        float pUOffset, float pVOffset,
        float pWidth, float pHeight,
        float pTextureWidth, float pTextureHeight
    ) {
        // No-op: remove the tv_frame.png scanline effect
    }
}