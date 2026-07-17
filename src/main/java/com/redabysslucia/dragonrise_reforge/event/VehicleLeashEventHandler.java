package com.redabysslucia.dragonrise_reforge.event;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleLeashData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber
public class VehicleLeashEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (!(target instanceof VehicleEntity vehicle)) return;

        ItemStack stack = player.getItemInHand(event.getHand());
        boolean isLead = stack.is(Items.LEAD);
        boolean isChain = stack.is(Items.CHAIN);
        if (!isLead && !isChain) return;
        if (player.getVehicle() == vehicle) return;

        if (player.level().isClientSide) {
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 1. 生物 → 载具（仅栓绳）
        if (isLead) {
            List<Mob> leashedMobs = serverLevel.getEntitiesOfClass(Mob.class,
                    vehicle.getBoundingBox().inflate(10),
                    mob -> mob.getLeashHolder() == player);
            if (!leashedMobs.isEmpty()) {
                leashedMobs.get(0).setLeashedTo(vehicle, true);
                if (!player.isCreative()) stack.shrink(1);
                VehicleLeashData.PLAYER_SELECTED_VEHICLE.remove(player.getUUID());
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        // 2. 载具 → 载具
        UUID selectedUUID = VehicleLeashData.PLAYER_SELECTED_VEHICLE.get(player.getUUID());
        if (selectedUUID != null && !selectedUUID.equals(vehicle.getUUID())) {
            Entity selected = serverLevel.getEntity(selectedUUID);
            double dist = vehicle.distanceToSqr(selected);
            if (selected instanceof VehicleEntity && (isChain || dist <= 100)) {
                vehicle.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.of(selectedUUID));
                vehicle.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, isChain);
                VehicleLeashData.PLAYER_SELECTED_VEHICLE.remove(player.getUUID());
                if (!player.isCreative()) stack.shrink(1);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        // 3. Shift剪断
        if (player.isShiftKeyDown() && vehicle.getEntityData().get(VehicleLeashData.LEASH_HOLDER).isPresent()) {
            vehicle.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.empty());
            vehicle.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, false);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        // 4. 选中/取消选中
        if (selectedUUID != null && selectedUUID.equals(vehicle.getUUID())) {
            VehicleLeashData.PLAYER_SELECTED_VEHICLE.remove(player.getUUID());
        } else {
            VehicleLeashData.PLAYER_SELECTED_VEHICLE.put(player.getUUID(), vehicle.getUUID());
        }
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }
}
