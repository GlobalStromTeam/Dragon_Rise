package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.inventory.menu.SmallVehicleContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Small 容器菜单从 3×9 改为 3×7 布局（21 格）。
 * addVehicleInventory 中常量 9 只出现两处：列数上限与槽位 index 乘数，一并替换为 7。
 */
@Mixin(value = SmallVehicleContainerMenu.class, remap = false)
public abstract class SmallVehicleContainerMenuMixin {

    @ModifyConstant(method = "addVehicleInventory", constant = @Constant(intValue = 9))
    private int dragonrise$sevenColumns(int original) {
        return 7;
    }
}
