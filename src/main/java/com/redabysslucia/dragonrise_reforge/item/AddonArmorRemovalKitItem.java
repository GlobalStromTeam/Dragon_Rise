package com.redabysslucia.dragonrise_reforge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 附加装甲拆卸套件：右键已安装附加装甲的载具即可拆下它，载具最大生命值回归基础值，
 * 拆下的装甲以 1 个 {@link AddonArmorKitItem} 的形式回收进玩家背包。
 *
 * <p>工具本体不消耗（耐久也不掉）；具体交互在 {@code events.AddonArmorInteraction} 里统一处理。</p>
 */
public class AddonArmorRemovalKitItem extends Item {

    public AddonArmorRemovalKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.dragonrise_reforge.addon_armor_removal_kit").withStyle(ChatFormatting.GRAY));
    }
}
