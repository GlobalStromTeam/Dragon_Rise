package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.overlay.RenderContext;
import com.atsuishio.superbwarfare.client.overlay.VehicleHudOverlay;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redabysslucia.dragonrise_reforge.client.EngineKeyHandler;
import com.redabysslucia.dragonrise_reforge.entity.vehicle.engine.EngineControl;
import com.redabysslucia.dragonrise_reforge.init.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VehicleHudOverlay.class, remap = false)
public abstract class VehicleEngineHudMixin {
    @Unique private static final int DRAGONRISE_ENGINE_X = 84;
    @Unique private static final int DRAGONRISE_BAR_WIDTH = 32;

    @Shadow
    private int getArmorPlateCompatHeight(Player player) {
        throw new AssertionError();
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/atsuishio/superbwarfare/client/overlay/VehicleHudOverlay;renderWeaponInfo(Lnet/minecraft/client/gui/GuiGraphics;Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;II)V"))
    private void dragonrise$drawEngineBesideHealth(RenderContext context, CallbackInfo ci) {
        var player = context.getPlayer();
        if (!dragonrise$showsEngine(player)) return;
        var vehicle = (VehicleEntity) player.getVehicle();
        var graphics = context.getGuiGraphics();
        var font = Minecraft.getInstance().font;
        var key = ModKeyMappings.ENGINE_TOGGLE.getBoundKey();
        int y = context.getScreenHeight() - 12 - getArmorPlateCompatHeight(player);
        int bar = DRAGONRISE_ENGINE_X + Math.max(8, font.width(key)) + 2;
        float progress = switch (EngineControl.state(vehicle)) {
            case EngineControl.STARTING -> EngineKeyHandler.startProgress(vehicle, context.getPartialTick());
            case EngineControl.RUNNING -> 1f;
            default -> 0f;
        };
        graphics.fill(bar, y, bar + DRAGONRISE_BAR_WIDTH, y + 6, 0xB0101010);
        graphics.fill(bar + 1, y + 1, bar + 1 + (int) (30 * progress), y + 5, 0xFF7AD646);
        graphics.drawString(font, key, DRAGONRISE_ENGINE_X, y - 1, 0xFFFFFF, false);
        RenderSystem.enableBlend();
    }

    @ModifyArg(method = "renderHoverInfo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
            remap = true), index = 2)
    private int dragonrise$shiftHoverHint(int x) {
        var mc = Minecraft.getInstance();
        if (!dragonrise$showsEngine(mc.player)) return x;
        var key = ModKeyMappings.ENGINE_TOGGLE.getBoundKey();
        return Math.max(x, DRAGONRISE_ENGINE_X + Math.max(8, mc.font.width(key)) + 2 + DRAGONRISE_BAR_WIDTH + 4);
    }

    @Unique
    private static boolean dragonrise$showsEngine(Player player) {
        return player != null && player.getVehicle() instanceof VehicleEntity vehicle
                && vehicle.getFirstPassenger() == player && EngineControl.appliesTo(vehicle);
    }
}
