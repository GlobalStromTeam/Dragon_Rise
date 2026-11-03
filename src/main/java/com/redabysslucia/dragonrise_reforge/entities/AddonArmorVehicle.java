package com.redabysslucia.dragonrise_reforge.entities;

/**
 * 可安装「附加装甲」的载具。
 *
 * <p>只有实现了本接口的载具才会接受：
 * <ul>
 *     <li>{@code dragonrise_reforge:addon_armor_kit}（附加装甲套件）右键安装；</li>
 *     <li>{@code dragonrise_reforge:addon_armor_removal_kit}（附加装甲拆卸套件）右键拆卸。</li>
 * </ul>
 *
 * <p>安装状态必须同时满足两点：写进载具 NBT（持久化）与写进同步数据位（客户端渲染骨骼要用）。
 * 安装后载具的最大生命值按 {@link #ADDON_ARMOR_MAX_HEALTH_BONUS} 提升，拆卸后回归。</p>
 *
 * @see com.redabysslucia.dragonrise_reforge.entities.utils.AddonArmorHelper
 */
public interface AddonArmorVehicle {

    /** 安装附加装甲后最大生命值的提升比例（10%）。 */
    float ADDON_ARMOR_MAX_HEALTH_BONUS = 0.10f;

    /** 载具当前是否安装着附加装甲。 */
    boolean hasAddonArmor();

    /** 设置附加装甲安装状态（只改同步数据位，NBT 由各载具的 save/load 负责）。 */
    void setAddonArmor(boolean installed);
}
