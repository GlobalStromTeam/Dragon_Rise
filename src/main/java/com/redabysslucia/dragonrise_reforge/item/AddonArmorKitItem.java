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
 * 附加装甲套件：右键受支持的载具即可为它安装附加装甲（最大生命值 +10%）。
 *
 * <p>安装成功后消耗 1 个；拆卸（用 {@link AddonArmorRemovalKitItem}）后会回收 1 个。
 * 具体交互在 {@code events.AddonArmorInteraction} 里统一处理，这里只负责物品本身与悬浮提示。</p>
 */
public class AddonArmorKitItem extends Item {

    public AddonArmorKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.dragonrise_reforge.addon_armor_kit").withStyle(ChatFormatting.GRAY));
    }
}
