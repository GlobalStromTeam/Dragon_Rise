package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.DestroyableProjectile;
import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AirBomb500kgEntity extends DestroyableProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int explosionTimer = -1;
    private static final int EXPLOSION_DELAY = 40; // 20 ticks = 1 second

    public AirBomb500kgEntity(EntityType<? extends AirBomb500kgEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        setExplosionRadius(20);
        setExplosionDamage(800);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        var entity = source.getDirectEntity();
        if (entity instanceof AirBomb500kgEntity bombEntity && bombEntity.getOwner() == this.getOwner()) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return com.atsuishio.superbwarfare.init.ModItems.MEDIUM_AERIAL_BOMB.get();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity == this.getOwner() || (this.getOwner() != null && entity == this.getOwner().getVehicle()) || entity instanceof AirBomb500kgEntity)
            return;
        if (this.level() instanceof ServerLevel) {
            if (ExplosionConfig.EXPLOSION_DESTROY.get() && ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()) {
                AABB aabb = new AABB(result.getLocation(), result.getLocation()).inflate(5);
                BlockPos.betweenClosedStream(aabb).forEach((pos) -> {
                    float hard = this.level().getBlockState(pos).getBlock().defaultDestroyTime();
                    if (hard != -1 && new Vec3(pos.getX(), pos.getY(), pos.getZ()).distanceTo(result.getLocation()) < 3) {
                        this.level().destroyBlock(pos, true);
                    }
                });
            }

            // 开始爆炸计时器
            startExplosionTimer();
        }
    }

    @Override
    public void onHitBlock(@NotNull BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level() instanceof ServerLevel) {
            if (ExplosionConfig.EXPLOSION_DESTROY.get() && ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()) {
                AABB aabb = new AABB(blockHitResult.getLocation(), blockHitResult.getLocation()).inflate(5);
                BlockPos.betweenClosedStream(aabb).forEach((pos) -> {
                    float hard = this.level().getBlockState(pos).getBlock().defaultDestroyTime();
                    if (hard != -1 && new Vec3(pos.getX(), pos.getY(), pos.getZ()).distanceTo(blockHitResult.getLocation()) < 3) {
                        this.level().destroyBlock(pos, true);
                    }
                });
            }

            // 开始爆炸计时器
            startExplosionTimer();
        }
    }

    private void startExplosionTimer() {
        if (explosionTimer == -1) {
            explosionTimer = EXPLOSION_DELAY;
            // 播放引信声音
            if (level() instanceof ServerLevel serverLevel) {

            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 处理爆炸计时器
        if (explosionTimer > 0) {
            explosionTimer--;
            
            // 计时器结束，触发爆炸
            if (explosionTimer <= 0) {
                if (level() instanceof ServerLevel) {
                    causeExplode(position());
                }
                this.discard();
                return;
            }
        }

        // 只有在没有启动爆炸计时器时才应用速度阻尼
        if (explosionTimer == -1) {
            // 阻尼默认1
            float customFriction = 0.99F;
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.scale(1 / customFriction));
        } else {
            // 爆炸倒计时期间停止移动
            this.setDeltaMovement(0, 0, 0);
        }
    }

    private PlayState movementPredicate(AnimationState<AirBomb500kgEntity> event) {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.airbomb500kg.start"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 0, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public @NotNull SoundEvent getSound() {
        return ModSounds.SHELL_FLY.get();
    }

    @Override
    public float getVolume() {
        return 0.7f;
    }

    @Override
    public float getMaxHealth() {
        return 50;
    }
}