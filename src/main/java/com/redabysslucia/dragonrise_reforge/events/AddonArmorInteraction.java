package com.redabysslucia.dragonrise_reforge.events;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.AddonArmorVehicle;
import com.redabysslucia.dragonrise_reforge.entities.utils.AddonArmorHelper;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 附加装甲套件 / 拆卸套件的右键交互。
 *
 * <p>只接管「对实现了 {@link AddonArmorVehicle} 的载具右键」这一种情况：</p>
 * <ul>
 *     <li>手持附加装甲套件 → 安装（非创造模式消耗 1 个），最大生命值 +10%；</li>
 *     <li>手持附加装甲拆卸套件 → 拆卸，最大生命值回归，回收 1 个套件进背包。</li>
 * </ul>
 *
 * <p><b>为什么两个事件都要管、而且客户端也要取消：</b>原版客户端右键实体会先发
 * {@code INTERACT_AT} 包、再走一次 {@code MultiPlayerGameMode.interact} 发 {@code INTERACT} 包，
 * 而这两条路径在<b>客户端本地</b>都会调用一次 {@code Player.interactOn} /
 * {@code entity.interactAt} —— 载具的 {@code interact} 里就是 {@code player.startRiding(this)}。
 * 只在服务端取消的话，客户端会先把玩家本地挂到车上（幽灵上车），服务端再拒绝，表现就是
 * 「手持套件右键还是上了车」。所以两个事件都要在两端取消：
 * 客户端取消只为了拦下本地 {@code startRiding}，真正的安装/拆卸只在服务端做。
 * {@code INTERACT_AT} 包在客户端取消之前就已经发出去了，服务端仍然能收到。</p>
 *
 * <p>两条路径可能都到服务端，用 {@link #LAST_HANDLED_TICK} 做同 tick 去重，避免重复安装和提示刷屏。</p>
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddonArmorInteraction {

    /** 玩家 UUID → 上次真正执行安装/拆卸的 tickCount，用于同一个右键动作的去重。 */
    private static final Map<UUID, Integer> LAST_HANDLED_TICK = new HashMap<>();

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (tryHandle(event.getEntity(), event.getTarget(), event.getItemStack(), event.getLevel())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (tryHandle(event.getEntity(), event.getTarget(), event.getItemStack(), event.getLevel())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /**
     * @return true 表示这次点击由本功能接管（调用方应取消事件，避免继续走「右键上车」）
     */
    private static boolean tryHandle(Player player, Entity target, ItemStack stack, Level level) {
        if (!(target instanceof VehicleEntity vehicle) || !(target instanceof AddonArmorVehicle armor)) {
            return false;
        }

        boolean installing = stack.is(ModItems.ADDON_ARMOR_KIT.get());
        boolean removing = stack.is(ModItems.ADDON_ARMOR_REMOVAL_KIT.get());
        if (!installing && !removing) {
            return false;
        }

        // 客户端只负责拦下本地的 startRiding，不做实际修改
        if (level.isClientSide()) {
            return true;
        }

        // 同一个右键动作可能同时触发 INTERACT_AT 和 INTERACT 两条服务端路径，只处理一次
        int tick = player.tickCount;
        Integer last = LAST_HANDLED_TICK.get(player.getUUID());
        if (last != null && last == tick) {
            return true;
        }
        LAST_HANDLED_TICK.put(player.getUUID(), tick);

        if (installing) {
            if (armor.hasAddonArmor()) {
                message(player, "message.dragonrise_reforge.addon_armor.already_installed");
            } else if (AddonArmorHelper.install(vehicle)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                vehicle.level().playSound(null, vehicle.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON,
                        SoundSource.NEUTRAL, 1.0F, 0.9F);
                message(player, "message.dragonrise_reforge.addon_armor.installed");
            }
        } else {
            if (!armor.hasAddonArmor()) {
                message(player, "message.dragonrise_reforge.addon_armor.not_installed");
            } else if (AddonArmorHelper.uninstall(vehicle)) {
                ItemStack recovered = new ItemStack(ModItems.ADDON_ARMOR_KIT.get());
                if (!player.getInventory().add(recovered)) {
                    player.drop(recovered, false);
                }
                vehicle.level().playSound(null, vehicle.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.NEUTRAL, 1.0F, 1.0F);
                message(player, "message.dragonrise_reforge.addon_armor.removed");
            }
        }
        return true;
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }
}
