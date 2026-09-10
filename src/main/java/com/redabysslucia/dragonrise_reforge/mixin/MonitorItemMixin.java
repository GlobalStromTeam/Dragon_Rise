package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * MonitorItem 兼容 mixin：
 * SBW 的 MonitorItem.inventoryTick 用 findDrone（instanceof DroneEntity）判断链接实体，
 * 对本项目原版实体实现的侦察无人车会返回 null，从而把 Using 复位、视角恢复，导致无法遥控。
 * 这里在手持选中且链接实体是本项目无人车时跳过该复位逻辑（未选中时保留原复位行为）。
 */
@Mixin(MonitorItem.class)
public class MonitorItemMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void dr$keepUsingForR6Drone(ItemStack stack, Level level, Entity entity,
                                        int slot, boolean selected, CallbackInfo ci) {
        if (!selected) return;   // 切走物品：保留原复位逻辑
        if (!stack.is(ModItems.MONITOR.get())) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING)) return;

        String linked = tag.getString(MonitorItem.LINKED_DRONE);
        if (com.redabysslucia.dragonrise_reforge.client.R6DroneClientLookup.findDrone(level, linked) != null) {
            ci.cancel();   // 我们的无人车：跳过 Using 复位（客户端/服务端都生效）
        }
    }
}
