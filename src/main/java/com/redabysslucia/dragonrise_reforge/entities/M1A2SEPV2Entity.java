package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class M1A2SEPV2Entity extends VehicleEntity {

    // 定义涂装状态同步数据 - 0=标准涂装, 1=沙漠涂装, 2=森林涂装 (可根据需要修改)
    public static final EntityDataAccessor<Integer> CAMO_TYPE =
            SynchedEntityData.defineId(M1A2SEPV2Entity.class, EntityDataSerializers.INT);

    // 喷漆罐物品缓存
    private Item sprayCanItem = null;

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
        // 初始化涂装数据，默认使用标准涂装 (0)
        this.entityData.define(CAMO_TYPE, 0);
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        // 保存涂装状态到NBT
        compound.putInt("CamoType", this.entityData.get(CAMO_TYPE));
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // 从NBT读取涂装状态
        if (compound.contains("CamoType")) {
            this.entityData.set(CAMO_TYPE, compound.getInt("CamoType"));
        }
    }

    @Override
    @Nonnull
    public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 延迟加载喷漆罐物品
        if (sprayCanItem == null) {
            sprayCanItem = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("dragonrise_reforge", "spray_can")
            );
        }

        // 检查是否为喷漆罐
        if (sprayCanItem != null && stack.getItem() == sprayCanItem) {
            // 切换涂装类型（0→1→2→0 循环，可根据需要调整涂装数量）
            int currentType = this.entityData.get(CAMO_TYPE);
            int newType = (currentType + 1) % 2;  // 如果有更多涂装，修改这里的数字
            this.entityData.set(CAMO_TYPE, newType);

            // 播放交互音效
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    this.getSoundSource(),
                    0.8F, 1.2F);

            // 返回成功结果
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 其他交互交给父类处理
        return super.interact(player, hand);
    }

    /**
     * 获取当前涂装类型
     * @return 0=标准涂装, 1=沙漠涂装, 2=森林涂装
     */
    public int getCamoType() {
        return this.entityData.get(CAMO_TYPE);
    }

    /**
     * 判断是否为沙漠涂装
     */
    public boolean isDesertCamo() {
        return this.entityData.get(CAMO_TYPE) == 1;
    }

    /**
     * 设置涂装类型
     * @param type 涂装类型ID
     */
    public void setCamoType(int type) {
        this.entityData.set(CAMO_TYPE, Math.max(0, Math.min(2, type))); // 限制范围
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