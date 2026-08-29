package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.RangeTool;
import com.redabysslucia.dragonrise_reforge.entities.M270Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.overlay.ForgeGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

/**
 * M270 即时弹道读数。射程与 SBW 迫击炮 HUD 相同：{@link RangeTool#getRange}。
 */
@OnlyIn(Dist.CLIENT)
public class M270BallisticOverlay implements IGuiOverlay {
    public static final String ID = Mod.MODID + "_m270_ballistic";

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        Player player = mc.player;
        if (player == null) return;

        if (!(player.getVehicle() instanceof M270Entity m270)) return;

        double elevation = -m270.getTurretXRot();

        Vec3 lookVec = m270.getLookAngle();
        float vehicleYaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float turretWorldYaw = vehicleYaw + m270.getTurretYRot();
        float worldAzimuth = (turretWorldYaw + 180f) % 360f;
        if (worldAzimuth < 0) worldAzimuth += 360f;

        // 武器索引 0；与座位玩家接口二选一，M270 固定用座位弹道
        double velocity = m270.getProjectileVelocity(0);
        double gravity = m270.getProjectileGravity(0);

        double range = (gravity > 0 && velocity > 0 && elevation > 0)
                ? RangeTool.getRange(elevation, velocity, gravity)
                : 0.0D;

        int x = 5;
        int y = screenHeight / 2 - 20;
        int color = 0xFFFFAA00;

        guiGraphics.drawString(mc.font,
                Component.literal("仰角: " + FormatTool.format1D(elevation, "\u00B0")),
                x, y, color, false);

        guiGraphics.drawString(mc.font,
                Component.literal(String.format("方位: %.1f\u00B0", worldAzimuth)),
                x, y + 12, color, false);

        String rangeStr = range > 0
                ? FormatTool.format0D(Math.max(0, (int) range), "m")
                : "---m";
        guiGraphics.drawString(mc.font,
                Component.literal("距离: " + rangeStr),
                x, y + 24, color, false);
    }
}
