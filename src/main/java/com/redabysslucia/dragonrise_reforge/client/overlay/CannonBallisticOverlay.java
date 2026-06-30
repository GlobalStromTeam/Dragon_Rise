package com.redabysslucia.dragonrise_reforge.client.overlay;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.BMP3Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;
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
import net.minecraftforge.registries.ForgeRegistries;

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

        // 获取当前武器名，检查是否为100MM主炮
        int seatIndex = v.getSeatIndex(player);
        String gunName = v.getGunName(seatIndex);
        if (!"100MM_Cannon".equals(gunName)) return;

        // 获取当前武器数据
        GunData gunData = v.getGunData(player);
        if (gunData == null) return;

        // 检查当前弹种是否为榴弹（通过弹药物品ID判断）
        var ammoStack = gunData.selectedAmmoConsumer().stack();
        var ammoId = ForgeRegistries.ITEMS.getKey(ammoStack.getItem());
        if (!"superbwarfare".equals(ammoId.getNamespace()) || !ammoId.getPath().contains("he")) return;

        // 仰角（度）
        float elevation = -v.getTurretXRot();
        // 世界方位角：通过载具朝向向量反算世界yaw + 炮塔相对旋转，转为0°=北 顺时针递增
        Vec3 lookVec = v.getLookAngle();
        float vehicleYaw = (float) Math.toDegrees(Math.atan2(-lookVec.x, lookVec.z));
        float turretWorldYaw = vehicleYaw + v.getTurretYRot();
        float worldAzimuth = (turretWorldYaw + 180f) % 360f;
        if (worldAzimuth < 0) worldAzimuth += 360f;

        // 获取弹道数据
        float velocity = gunData.get(GunProp.VELOCITY).floatValue();
        float gravity = gunData.get(GunProp.GRAVITY).floatValue();

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
     * 炮弹实体每帧逻辑：position += velocity; velocity.y -= gravity
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