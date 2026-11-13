package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.AddonArmorVehicle;

/**
 * 附加装甲的安装 / 拆卸与最大生命值换算。
 *
 * <p>superb 的 {@code VehicleEntity.getMaxHealth()} 直接返回载具数据里的 {@code MaxHealth}，
 * 所以「+10% 上限」不改数据文件，而是在各载具实体里覆写 {@code getMaxHealth()}：
 * {@code return AddonArmorHelper.maxHealth(super.getMaxHealth(), installed);}
 * 这样 {@code health} 的 setter 会自动按新上限 clamp，拆卸后血量自然回落到基础上限。</p>
 *
 * <p>安装时把增加的那部分上限同时补进当前血量，避免刚装好就显示成「掉了 10% 血」；
 * 拆卸时只 clamp 当前血量，不扣血。</p>
 */
public final class AddonArmorHelper {

    private AddonArmorHelper() {
    }

    /** 按是否安装附加装甲换算最大生命值。 */
    public static float maxHealth(float baseMaxHealth, boolean installed) {
        return installed ? baseMaxHealth * (1.0f + AddonArmorVehicle.ADDON_ARMOR_MAX_HEALTH_BONUS) : baseMaxHealth;
    }

    /**
     * 安装附加装甲。
     *
     * @return true 表示本次安装成功；载具不支持或已经装过则返回 false
     */
    public static boolean install(VehicleEntity vehicle) {
        if (!(vehicle instanceof AddonArmorVehicle armor) || armor.hasAddonArmor()) {
            return false;
        }
        float oldMax = vehicle.getMaxHealth();
        float health = vehicle.getHealth();

        armor.setAddonArmor(true);

        float newMax = vehicle.getMaxHealth();
        vehicle.setHealth(Math.min(newMax, health + (newMax - oldMax)));
        return true;
    }

    /**
     * 拆卸附加装甲。
     *
     * @return true 表示本次拆卸成功；载具不支持或本来就没装则返回 false
     */
    public static boolean uninstall(VehicleEntity vehicle) {
        if (!(vehicle instanceof AddonArmorVehicle armor) || !armor.hasAddonArmor()) {
            return false;
        }
        armor.setAddonArmor(false);
        float newMax = vehicle.getMaxHealth();
        vehicle.setHealth(Math.min(vehicle.getHealth(), newMax));
        return true;
    }
}
