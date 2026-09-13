package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MonitorItem 兼容 mixin（<b>服务端侧</b>）：
 * <p>
 * SBW 的 {@code MonitorItem.inventoryTick} 在「选中 + Using=true + findDrone(...)==null」时
 * 会把 {@code Using} 复位为 false（findDrone 只认 SBW 自家的 DroneEntity，本项目无人车必然为 null）。
 * 该方法**在客户端与服务端两个线程都会执行**：
 * <ul>
 *   <li>专用服务器：{@link MonitorItemMixin} 注册在 mixins.json 的 client 列表，服务端不加载 →
 *       服务端把 Using 置 false 后同步给客户端，表现为「刚进入无人车操作界面就被弹出」；</li>
 *   <li>单机（集成服务器）：客户端 mixin 会作用于服务端线程，但客户端查找器要求 ClientLevel，
 *       服务端线程传入 ServerLevel 时查不到 → 同样不再拦截。</li>
 * </ul>
 * 因此这里注册到 <b>common（mixins）列表</b>，用纯服务端的
 * {@link R6DroneEntity#findServerDrone(ServerLevel, String)} 拦截复位。
 * <p>
 * 客户端线程（ClientLevel）在此直接放行，由 client 列表的 {@link MonitorItemMixin}
 * 用 R6DroneClientLookup（含每帧缓存）处理。
 */
@Mixin(MonitorItem.class)
public class MonitorItemServerMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void dr$keepUsingForR6DroneServer(ItemStack stack, Level level, Entity entity,
                                              int slot, boolean selected, CallbackInfo ci) {
        if (!selected) return;   // 切走物品：保留原复位逻辑（相机需恢复）
        if (!(level instanceof ServerLevel serverLevel)) return;   // 客户端线程交给 MonitorItemMixin
        if (!stack.is(ModItems.MONITOR.get())) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(MonitorItem.USING)) return;

        String linked = tag.getString(MonitorItem.LINKED_DRONE);
        if (R6DroneEntity.findServerDrone(serverLevel, linked) != null) {
            ci.cancel();   // 本项目无人车：跳过 Using 复位，否则服务端同步 false 会把玩家弹出操作界面
        }
    }
}
