package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.CustomExplosion;
import com.atsuishio.superbwarfare.tools.DamageHandler;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AAshellEntity extends FastThrowableProjectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);



    public AAshellEntity(EntityType<? extends AAshellEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        setExplosionDamage(80f);
        setExplosionRadius(15f);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.SMALL_SHELL_AA.get();
    }

    @Override
    public void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (this.getOwner() != null && this.getOwner().getVehicle() != null && entity == this.getOwner().getVehicle())
            return;
        if (this.level() instanceof ServerLevel) {
            DamageHandler.doDamage(entity, ModDamageTypes.causeProjectileHitDamage(this.level().registryAccess(), this, this.getOwner()), getExplosionDamageValue());

            if (entity instanceof LivingEntity) {
                entity.invulnerableTime = 0;
            }

            if (this.tickCount > 0) {
                causeExplode(result.getLocation(), true);
            }
            this.discard();
        }
    }

    @Override
    public void onHitBlock(@NotNull BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        BlockPos resultPos = blockHitResult.getBlockPos();
        BlockState state = this.level().getBlockState(resultPos);

        if (this.level() instanceof ServerLevel) {
            float hardness = this.level().getBlockState(resultPos).getBlock().defaultDestroyTime();
            if (hardness != -1) {
                if (ExplosionConfig.EXPLOSION_DESTROY.get() && ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()) {
                    boolean destroy = Math.random() < Mth.clamp(1 - (hardness / 50), 0.1, 1);
                    if (destroy) {
                        this.level().destroyBlock(resultPos, true);
                    }
                }
            }
        }

        if (state.getBlock() instanceof BellBlock bell) {
            bell.attemptToRing(this.level(), resultPos, blockHitResult.getDirection());
        }
        if (this.level() instanceof ServerLevel) {
            causeExplode(blockHitResult.getLocation(), false);
        }
        this.discard();
    }

    private void causeExplode(Vec3 vec3, boolean hitEntity) {
        new CustomExplosion.Builder(this)
                .attacker(this.getOwner())
                .damage(getExplosionDamageValue() * 1.25F)
                .radius(getExplosionRadiusValue())
                .position(vec3)
                .withParticleType(ParticleTool.particleTypeForRadius(getExplosionRadiusValue()))
                .destroyBlock(!hitEntity)
                .explode();
    }

    @Override
    public void tick() {
        super.tick();
        if (getOwner() != null && distanceToSqr(getOwner()) > 1048576) {
            if (level() instanceof ServerLevel) {
                causeExplode(position());
            }
            this.discard();
        }

        if (level() instanceof ServerLevel) {
            double radius = 8.0;
            for (Entity entity : level().getEntities(this, getBoundingBox().inflate(radius))) {
                // 排除自己和发射者
                if (entity != this && entity != getOwner() && !entity.onGround()) {
                    // 检测到不在地面上的实体，触发爆炸
                    causeExplode(position());
                    this.discard();
                    break;
                }
            }
        }
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isFastMoving() {
        return false;
    }

    @Override
    public boolean forceLoadChunk() {
        return true;
    }
}