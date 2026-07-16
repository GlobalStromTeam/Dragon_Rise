package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class ZTZ99AEntity extends GeoVehicleEntity{

    private final Float[][] PitchAdjustments = {
            {180f, 180f, 180f, 4f, -4f},
            {-180f, -180f, 180f, 4f, -4f},
    };

    public static final EntityDataAccessor<Integer> CAMO_TYPE =
            SynchedEntityData.defineId(ZTZ99AEntity.class, EntityDataSerializers.INT);

    // 撬棍物品缓存
    private Item crowbarItem = null;

    public ZTZ99AEntity(EntityType<?> pEntityType, Level pLevel) {
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

        // 延迟加载撬棍物品
        if (crowbarItem == null) {
            crowbarItem = ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("dragonrise_reforge", "spray_can")
            );
        }

        // 检查是否为撬棍
        if (crowbarItem != null && stack.getItem() == crowbarItem) {
            // 切换涂装类型（0→1→2→0循环）
            int currentType = this.entityData.get(CAMO_TYPE);
            int newType = (currentType + 1) % 2;
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
     */
    public int getCamoType() {
        return this.entityData.get(CAMO_TYPE);
    }

    /**
     * 判断是否为NATO涂装
     */
    public boolean isNatoCamo() {
        return this.entityData.get(CAMO_TYPE) == 1;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
    }

    private PlayState cannonFirePredicate(AnimationState<ZTZ99AEntity> event) {
        if (getShootAnimationTimer(0, 0) > 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.ztz99a.fire"));
        }
        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.ztz99a.idle"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
    }



//    @Override
//    public int getTrackAnimationLength() {
//        return 80;
//    }
//
//    @Override
//    public float getTurretMaxHealth() {
//        return 100;
//    }
//
//    @Override
//    public float getWheelMaxHealth() {
//        return 100;
//    }
//
//    @Override
//    public float getEngineMaxHealth() {
//        return 150;
//    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public ResourceLocation getBackgroundTexture() {
//        var mc = net.minecraft.client.Minecraft.getInstance();
//        var player = mc.player;
//        if (player == null) return null;
//
//        var seatIndex = getSeatIndex(player);
//        // 一号位背景
//        if (seatIndex == 0) {
//            return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos3.png");
//        }
//        // 二号位背景
//        else if (seatIndex == 1) {
//            return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos2.png");
//        }
//        return null;
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean shouldRenderBackground() {
//        // 只在一号位和二号位显示背景
//        var mc = net.minecraft.client.Minecraft.getInstance();
//        var player = mc.player;
//        if (player == null) return false;
//
//        var seatIndex = getSeatIndex(player);
//        // 一号位是索引0，二号位是索引1
//        return seatIndex == 0 || seatIndex == 1;
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public float getBackgroundAlpha() {
//        // 控制背景透明度 (0.0f - 1.0f)
//        return 1.0f;
//    }
}
