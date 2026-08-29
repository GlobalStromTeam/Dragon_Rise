package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.RangeTool;
import com.redabysslucia.dragonrise_reforge.entities.BMP3Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.overlay.ForgeGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * ZBD04A / BMP3 主炮榴弹即时弹道读数。
 * 射程算法与 SBW 迫击炮 HUD 一致：{@link RangeTool#getRange(double, double, double)}
 * （同高水平落点，R = v²·sin(2θ)/g）。
 */
@OnlyIn(Dist.CLIENT)
public class CannonBallisticOverlay implements IGuiOverlay {
    public static final String ID = Mod.MODID + "_cannon_ballistic";

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        Player player = mc.player;
        if (player == null) return;

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof ZBD04AEntity) && !(vehicle instanceof BMP3Entity)) return;

        var v = (VehicleEntity) vehicle;

        int seatIndex = v.getSeatIndex(player);
        String gunName = v.getGunName(seatIndex);
        if (!"100MM_Cannon".equals(gunName)) return;

        GunData gunData = v.getGunData(player);
        if (gunData == null) return;

        var ammoStack = gunData.selectedAmmoConsumer().stack();
        var ammoId = ForgeRegistries.ITEMS.getKey(ammoStack.getItem());
        if (ammoId == null || !"superbwarfare".equals(ammoId.getNamespace()) || !ammoId.getPath().contains("he")) {
            return;
        }

        // 炮塔仰角：与 SBW 载具惯例一致（turretXRot 抬头为负 → 取反得正仰角）
        double elevation = -v.getTurretXRot();

        // 世界方位：0°=北，顺时针
        Vec3 lookVec = v.getLookAngle();
        float vehicleYaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float turretWorldYaw = vehicleYaw + v.getTurretYRot();
        float worldAzimuth = (turretWorldYaw + 180f) % 360f;
        if (worldAzimuth < 0) worldAzimuth += 360f;

        // 与迫击炮/Type63 相同：座位当前武器的弹速与重力
        double velocity = v.getProjectileVelocity(player);
        double gravity = v.getProjectileGravity(player);

        // RangeTool.getRange(pitchDeg, velocity, gravity) — 同高弹道，不掺炮口高度
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

        // Type63/迫击炮：射程取整显示
        String rangeStr = range > 0
                ? FormatTool.format0D(Math.max(0, (int) range), "m")
                : "---m";
        guiGraphics.drawString(mc.font,
                Component.literal("距离: " + rangeStr),
                x, y + 24, color, false);
    }
}
