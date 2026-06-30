package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.redabysslucia.dragonrise_reforge.entities.M270Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class M270BallisticOverlay implements IGuiOverlay {
    public static final String ID = Mod.MODID + "_m270_ballistic";

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        Player player = mc.player;
        if (player == null) return;

        if (!(player.getVehicle() instanceof M270Entity m270)) return;

        // 仰角（度）
        float elevation = -m270.getTurretXRot();
        // 世界方位角：通过载具朝向向量反算世界yaw + 炮塔相对旋转，转为0°=北 顺时针递增
        Vec3 lookVec = m270.getLookAngle();
        float vehicleYaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float turretWorldYaw = vehicleYaw + m270.getTurretYRot();
        float worldAzimuth = (turretWorldYaw + 180f) % 360f;
        if (worldAzimuth < 0) worldAzimuth += 360f;

        // 获取火箭弹道数据
        GunData gunData = m270.getGunData(0, 0);
        float velocity = gunData != null ? gunData.get(GunProp.VELOCITY).floatValue() : 16f;
        float gravity = gunData != null ? gunData.get(GunProp.GRAVITY).floatValue() : 0.02f;

        // 计算落点距离（格）
        double range = calcRange(elevation, velocity, gravity);

        int x = 5;
        int y = screenHeight / 2 - 20;
        int color = 0xFFFFAA00;

        guiGraphics.drawString(mc.font,
                Component.literal(String.format("仰角: %.1f°", elevation)),
                x, y, color, false);

        guiGraphics.drawString(mc.font,
                Component.literal(String.format("方位: %.1f°", worldAzimuth)),
                x, y + 12, color, false);

        guiGraphics.drawString(mc.font,
                Component.literal(String.format("距离: %.1f 格", range)),
                x, y + 24, color, false);
    }

    /**
     * 弹道计算（离散物理，与炮弹实体 FastThrowableProjectile.tick() 一致）
     *
     * 火箭弹实体每帧逻辑：position += velocity; velocity.y -= gravity
     * 第一帧位置更新在重力生效前，因此离散公式比连续公式多一个 v*cos(θ) 项
     *
     * 公式：range = v² * sin(2θ) / g + v * cos(θ)
     */
    private double calcRange(double elevationDeg, double velocity, double gravity) {
        if (gravity <= 0 || elevationDeg <= 0) return 0;
        double theta = elevationDeg * Mth.DEG_TO_RAD;
        return velocity * velocity * Math.sin(2 * theta) / gravity + velocity * Math.cos(theta);
    }
}