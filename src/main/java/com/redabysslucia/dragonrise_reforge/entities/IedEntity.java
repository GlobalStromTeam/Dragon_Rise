package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.projectile.C4Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 土制遥控炸弹（IED）：官方遥控 C4（{@link C4Entity}）的换皮，多一个"引信倒计时"。
 * <p>
 * 手机右键后调用 {@link #armFuse(int)} 设定倒计时（= ied.ogg 的时长，202 tick），
 * 到 0 时引爆并补一声爆炸音效（玩家要求：铃声播完再爆炸）。
 * 爆炸参数完全复用官方 C4 的 {@code explode()}（在父类中是 final，正好不会被改坏）。
 */
@SuppressWarnings("removal")
public class IedEntity extends C4Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    /** 引信剩余 tick，<= 0 表示未布置 */
    private int fuse = -1;

    @SuppressWarnings("unchecked")
    public IedEntity(EntityType<?> type, Level level) {
        // C4Entity 的构造器签名是 EntityType<C4Entity>，运行时泛型擦除，强转安全
        super((EntityType<C4Entity>) type, level);
        // 遥控型：官方 C4Entity.tick() 只在 IS_CONTROLLABLE == false 时才走定时引信与蜂鸣，
        // 置 true 后放置期间完全静音，只由男孩牌手机激活时才播 ied.ogg。
        this.entityData.set(IS_CONTROLLABLE, true);
    }

    /** 由手机触发起爆倒计时 */
    public void armFuse(int ticks) {
        this.fuse = ticks;
    }

    public boolean fuseArmed() {
        return this.fuse > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.fuse > 0 && !this.level().isClientSide) {
            if (--this.fuse == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, 1.0F);
                this.explode();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("IedFuse", this.fuse);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.fuse = compound.getInt("IedFuse");
    }
}
