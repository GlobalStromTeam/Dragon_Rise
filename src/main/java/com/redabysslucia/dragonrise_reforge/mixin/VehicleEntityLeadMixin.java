package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleLeashData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = VehicleEntity.class, remap = false)
public abstract class VehicleEntityLeadMixin {

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void onDefineSynchedData(CallbackInfo ci) {
        var data = ((VehicleEntity) (Object) this).getEntityData();
        data.define(VehicleLeashData.LEASH_HOLDER, Optional.empty());
        data.define(VehicleLeashData.CHAIN_LEASHED, false);
    }

    // ==================== 保存/加载 ====================

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        self.getEntityData().get(VehicleLeashData.LEASH_HOLDER).ifPresent(uuid ->
                tag.putUUID("DRLeashHolder", uuid));
        tag.putBoolean("DRChainLeashed", self.getEntityData().get(VehicleLeashData.CHAIN_LEASHED));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        if (tag.hasUUID("DRLeashHolder")) {
            self.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.of(tag.getUUID("DRLeashHolder")));
            self.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, tag.getBoolean("DRChainLeashed"));
        }
    }

    // ==================== 交互 ====================

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        VehicleEntity self = (VehicleEntity) (Object) this;
        ItemStack stack = player.getItemInHand(hand);

        boolean isLead = stack.is(Items.LEAD);
        boolean isChain = stack.is(Items.CHAIN);
        if (!isLead && !isChain) return;
        if (player.getVehicle() == self) return;

        Level level = self.level();
        if (level.isClientSide) {
            cir.setReturnValue(InteractionResult.CONSUME);
            return;
        }

        // 1. 实体 → 载具（仅栓绳）
        if (isLead) {
            List<Mob> leashedMobs = level.getEntitiesOfClass(Mob.class,
                    self.getBoundingBox().inflate(10),
                    mob -> mob.getLeashHolder() == player);
            if (!leashedMobs.isEmpty()) {
                leashedMobs.get(0).setLeashedTo(self, true);
                if (!player.isCreative()) stack.shrink(1);
                VehicleLeashData.PLAYER_SELECTED_VEHICLE.remove(player.getUUID());
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
        }

        // 2. 载具 → 载具
        UUID selectedUUID = VehicleLeashData.PLAYER_SELECTED_VEHICLE.get(player.getUUID());
        if (selectedUUID != null && !selectedUUID.equals(self.getUUID())) {
            if (level instanceof ServerLevel serverLevel) {
                Entity selectedEntity = serverLevel.getEntity(selectedUUID);
                double dist = self.distanceToSqr(selectedEntity);
                // 铁链无距离限制，栓绳限 10 格
                if (selectedEntity instanceof VehicleEntity && (isChain || dist <= 100)) {
                    releaseLeash(self);
                    self.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.of(selectedUUID));
                    self.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, isChain);
                    VehicleLeashData.PLAYER_SELECTED_VEHICLE.remove(player.getUUID());
                    if (!player.isCreative()) stack.shrink(1);
                    cir.setReturnValue(InteractionResult.SUCCESS);
                    return;
                }
            }
        }

        // 3. 解除栓绳
        if (player.isShiftKeyDown() && self.getEntityData().get(VehicleLeashData.LEASH_HOLDER).isPresent()) {
            releaseLeash(self);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        // 4. 选中/取消选中
        if (selectedUUID != null && selectedUUID.equals(self.getUUID())) {
            VehicleLeashData.PLAYER_SELECTED_VEHICLE.remove(player.getUUID());
        } else {
            VehicleLeashData.PLAYER_SELECTED_VEHICLE.put(player.getUUID(), self.getUUID());
        }
        cir.setReturnValue(InteractionResult.CONSUME);
    }

    private void releaseLeash(VehicleEntity self) {
        self.getEntityData().set(VehicleLeashData.LEASH_HOLDER, Optional.empty());
        self.getEntityData().set(VehicleLeashData.CHAIN_LEASHED, false);
    }
}
