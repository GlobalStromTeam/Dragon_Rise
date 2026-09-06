package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SHIELDEntity extends DragonriseVehicleBase {

    private static final EntityDataAccessor<Float> TARGET_YAW =
            SynchedEntityData.defineId(SHIELDEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_PITCH =
            SynchedEntityData.defineId(SHIELDEntity.class, EntityDataSerializers.FLOAT);

    public SHIELDEntity(EntityType<SHIELDEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_YAW, this.getYRot());
        this.entityData.define(TARGET_PITCH, this.getXRot());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("TargetPitch", this.entityData.get(TARGET_PITCH));
        compound.putFloat("TargetYaw", this.entityData.get(TARGET_YAW));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TargetPitch")) {
            this.entityData.set(TARGET_PITCH, compound.getFloat("TargetPitch"));
        }
        if (compound.contains("TargetYaw")) {
            this.entityData.set(TARGET_YAW, compound.getFloat("TargetYaw"));
        }
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = super.interact(player, hand);
        if (result != InteractionResult.PASS) return result;

        if (player.isShiftKeyDown()) {
            this.entityData.set(TARGET_YAW, player.getYRot());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void travel() {
        super.travel();

        float diffY = Mth.wrapDegrees(this.entityData.get(TARGET_YAW) - this.getYRot());
        float diffX = Mth.wrapDegrees(this.entityData.get(TARGET_PITCH) - this.getXRot());

        this.setYRot(this.getYRot() + Mth.clamp(0.5f * diffY, -20f, 20f));
        this.setXRot(this.getXRot() + Mth.clamp(0.5f * diffX, -20f, 20f));
    }
}