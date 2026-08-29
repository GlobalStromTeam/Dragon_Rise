package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class M1A2SEPV2Entity extends VehicleEntity {

    // Tusk 套件安装状态（false=已拆卸/默认，true=已安装）；由左键近战点击 10 次切换
    public static final EntityDataAccessor<Boolean> TUSK_INSTALLED =
            SynchedEntityData.defineId(M1A2SEPV2Entity.class, EntityDataSerializers.BOOLEAN);

    private int tuskClickCount = 0;

    private final Float[][] PitchAdjustments = {
            {150f, 180f, 17f, 0f, -10f},
            {-150f, -180f, 17f, 0f, -10f},
    };

    public M1A2SEPV2Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // 默认拆卸状态
        builder.define(TUSK_INSTALLED, false);
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("TuskInstalled", isTuskInstalled());
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TuskInstalled")) {
            setTuskInstalled(compound.getBoolean("TuskInstalled"));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 玩家近战点击（直接伤害源=玩家自身）：计数 10 次切换 Tusk 套件，点击不造成伤害
        if (!level().isClientSide()
                && source.getEntity() instanceof Player player
                && source.getDirectEntity() == source.getEntity()) {
            tuskClickCount++;
            if (tuskClickCount >= 10) {
                tuskClickCount = 0;
                boolean installed = !isTuskInstalled();
                setTuskInstalled(installed);
                player.displayClientMessage(Component.translatable(
                        installed
                                ? "message.dragonrise_reforge.m1a2sepv2.tusk_installed"
                                : "message.dragonrise_reforge.m1a2sepv2.tusk_removed"), true);
                this.level().playSound(null, this.blockPosition(),
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        this.getSoundSource(), 0.8F, 1.2F);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    /**
     * Tusk 套件是否已安装（true=显示，false=隐藏/拆卸）
     */
    public boolean isTuskInstalled() {
        return this.entityData.get(TUSK_INSTALLED);
    }

    public void setTuskInstalled(boolean installed) {
        this.entityData.set(TUSK_INSTALLED, installed);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 100;
    }

    @Override
    public float getWheelMaxHealth() {
        return 100;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }
}
