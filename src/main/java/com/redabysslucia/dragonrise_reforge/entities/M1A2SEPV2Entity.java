package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.AddonArmorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * M1A2 SEPv2。
 *
 * <p>附加装甲（TUSK 套件）：
 * <ul>
 *     <li>是否安装同时写在 NBT（{@code TuskInstalled}）与同步数据位（渲染隐藏/显示
 *         {@code move_tusk} / {@code move_tuskbody} 两根骨骼）里；</li>
 *     <li>安装 / 拆卸走「附加装甲套件 / 附加装甲拆卸套件」物品，见
 *         {@code events.AddonArmorInteraction}；</li>
 *     <li>安装后最大生命值 +10%（覆写 {@link #getMaxHealth()}）。</li>
 * </ul>
 */
@SuppressWarnings("removal")
public class M1A2SEPV2Entity extends VehicleEntity implements AddonArmorVehicle {

    /** 附加装甲是否已安装（客户端渲染骨骼、服务端血量上限共用） */
    public static final EntityDataAccessor<Boolean> ADDON_ARMOR_INSTALLED =
            SynchedEntityData.defineId(M1A2SEPV2Entity.class, EntityDataSerializers.BOOLEAN);

    /** 见 {@link M1a2sepv1Entity} 里同名字段的注释：super 初始化 HEALTH 时本类同步位还未定义 */
    private boolean addonArmorDataReady;

    private final Float[][] PitchAdjustments = {
            {150f, 180f, 17f, 0f, -10f},
            {-150f, -180f, 17f, 0f, -10f},
    };

    public M1A2SEPV2Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ADDON_ARMOR_INSTALLED, false);
        this.addonArmorDataReady = true;
    }

    @Override
    public float getMaxHealth() {
        return AddonArmorHelper.maxHealth(super.getMaxHealth(),
                this.addonArmorDataReady && this.entityData.get(ADDON_ARMOR_INSTALLED));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("TuskInstalled", hasAddonArmor());
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("TuskInstalled")) {
            setAddonArmor(compound.getBoolean("TuskInstalled"));
        }
    }

    @Override
    public boolean hasAddonArmor() {
        return this.entityData.get(ADDON_ARMOR_INSTALLED);
    }

    @Override
    public void setAddonArmor(boolean installed) {
        this.entityData.set(ADDON_ARMOR_INSTALLED, installed);
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
