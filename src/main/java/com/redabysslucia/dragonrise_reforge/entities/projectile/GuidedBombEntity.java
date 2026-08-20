package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance;
import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.AerialBombEntity;
import com.atsuishio.superbwarfare.entity.projectile.BasicGeoProjectileEntity;
import com.atsuishio.superbwarfare.entity.projectile.MissileProjectile;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.github.mcmodderanchor.simplebedrockmodel.v2.common.model.runtime.BakedModelInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 制导炸弹基类（参照官方 AerialBombEntity 的炸弹行为 + MissileProjectile 的制导框架）。
 *
 * 继承 MissileProjectile 的关键收益：GunItem.shootBullet 发射时会对 MissileProjectile
 * 自动写入制导参数——
 *  - 锁定了地面再投弹：targetPos = 锁定坐标 → setGuideType(1) + setTargetVec(锁定点)（卫星制导）；
 *  - 未锁定直接投弹：targetPos 为空 → guideType 保持 0（激光架束制导）。
 *
 * 两种制导模式（均为"每 tick 向目标点轻微修正速度方向"，整体仍是弹道下落）：
 *  - 激光架束（guideType 0）：目标点 = 吊舱世界位置沿玩家视线的地形交点（实时变化）；
 *  - 卫星制导（guideType 1）：目标点 = 发射时锁定的地面坐标（固定）。
 *
 * 炸弹特性：有重力（配置 Gravity）、穿水、命中方块/实体即爆炸（参照 AerialBombEntity）、
 * 不受诱饵弹干扰、出膛 3 tick 免碰撞、强制加载自身区块（超远投弹）。
 * 渲染：实现 BasicGeoProjectileEntity，按实体注册名自动解析模型/贴图路径。
 */
public abstract class GuidedBombEntity extends MissileProjectile implements BasicGeoProjectileEntity {

    /** 每 tick 最大速度方向修正角（度）——"略微修正" */
    private static final double CORRECTION_DEGREES_PER_TICK = 1.0;

    /** 激光架束瞄准射线最大距离（格） */
    private static final double LASER_SEEK_DISTANCE = 1024.0;

    public GuidedBombEntity(EntityType<? extends GuidedBombEntity> type, Level level) {
        super(type, level);
        this.setExplosionDamage(1300f);
        this.setExplosionRadius(32f);
        this.setDistracted(false);
    }

    // ==================== 炸弹特性 ====================

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public float getCustomGravity() {
        return this.getGravityValue();
    }

    @Override
    public boolean canPassThroughFluid() {
        return true;
    }

    @Override
    public Item getDefaultItem() {
        return ModItems.LARGE_AERIAL_BOMB.get();
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.SHELL_FLY.get();
    }

    @Override
    public float getVolume() {
        return 0.7f;
    }

    @Override
    public float getMaxHealth() {
        return 90f;
    }

    /** 制导炸弹不应被热焰弹/诱饵干扰 */
    @Override
    public void distractedByDecoy() {
    }

    // ==================== 命中即爆（参照 AerialBombEntity） ====================

    @Override
    public void afterHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner = getOwner();
        if (entity == owner || (owner != null && entity == owner.getVehicle()) || entity instanceof AerialBombEntity) {
            return;
        }
        if (this.level() instanceof ServerLevel) {
            destroyNearbyBlocks(result.getLocation());
            causeExplode(result.getLocation());
            this.discard();
        }
    }

    @Override
    public void afterHitBlock(BlockHitResult result) {
        if (this.level() instanceof ServerLevel) {
            destroyNearbyBlocks(result.getLocation());
            causeExplode(result.getLocation());
            this.discard();
        }
    }

    private void destroyNearbyBlocks(Vec3 hitPos) {
        if (ExplosionConfig.EXPLOSION_DESTROY.get()
                && ExplosionConfig.EXTRA_EXPLOSION_EFFECT.get()
                && getExplosionDestroyValue()) {
            AABB aabb = new AABB(hitPos, hitPos).inflate(5.0);
            BlockPos.betweenClosedStream(aabb).forEach(pos -> {
                float hard = this.level().getBlockState(pos).getBlock().defaultDestroyTime();
                if (hard != -1f
                        && new Vec3(pos.getX(), pos.getY(), pos.getZ()).distanceTo(hitPos) < 3) {
                    this.level().destroyBlock(pos, true);
                }
            });
        }
    }

    // ==================== 制导 ====================

    @Override
    public void tick() {
        // MissileProjectile.tick（BVR 同步、失锁自毁）+ FastThrowableProjectile.tick（移动/重力/碰撞/爆炸）
        super.tick();

        if (level() instanceof ServerLevel && isAlive() && getOwner() != null) {
            Vec3 target;
            if (getGuideType() == 1) {
                // 卫星制导：向锁定的地面坐标修正
                target = getTargetPos();
            } else {
                // 激光架束制导：向吊舱当前瞄准点修正（实时）
                target = calculatePodAimPoint();
                if (target != null) {
                    setTargetPos(target);
                }
            }
            if (target != null) {
                correctVelocityTowards(target);
            }
        }
    }

    /**
     * 计算吊舱当前瞄准点：吊舱世界位置（ShootPos.ViewPosition 经载具变换）沿玩家视线
     * （服务端同步的 lookAngle，吊舱视角下即吊舱视线）与地形（COLLIDER）的交点。
     */
    private Vec3 calculatePodAimPoint() {
        Entity owner = getOwner();
        if (owner == null) {
            return null;
        }
        Vec3 podPos;
        Vec3 dir = owner.getLookAngle();
        if (owner.getVehicle() instanceof VehicleEntity vehicle) {
            // 吊舱世界位置 = ShootPos.ViewPosition 经载具变换（吊舱视角位置）
            podPos = vehicle.getViewPos(owner, 1.0f);
            if (podPos == null) {
                podPos = owner.getEyePosition();
            }
        } else {
            podPos = owner.getEyePosition();
        }
        BlockHitResult hit = level().clip(new ClipContext(
                podPos, podPos.add(dir.scale(LASER_SEEK_DISTANCE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        return hit.getType() == HitResult.Type.MISS
                ? podPos.add(dir.scale(LASER_SEEK_DISTANCE))
                : hit.getLocation();
    }

    /**
     * 将速度方向绕旋转轴向目标方向旋转，每 tick 最多 CORRECTION_DEGREES_PER_TICK 度，
     * 速度大小保持不变（罗德里格斯旋转）。
     */
    private void correctVelocityTowards(Vec3 target) {
        Vec3 cur = getDeltaMovement();
        if (cur.lengthSqr() < 1.0e-8) {
            return;
        }
        Vec3 curDir = cur.normalize();
        Vec3 toTarget = target.subtract(position()).normalize();

        double angle = Math.toDegrees(Math.acos(Mth.clamp(curDir.dot(toTarget), -1.0, 1.0)));
        if (angle < 0.01) {
            return;
        }

        double turn = Math.min(angle, CORRECTION_DEGREES_PER_TICK);
        Vec3 axis = curDir.cross(toTarget);
        double len = axis.length();
        if (len < 1.0e-8) {
            return;
        }
        axis = axis.scale(1.0 / len);

        double rad = Math.toRadians(turn);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        Vec3 rotated = cur.scale(cos)
                .add(axis.cross(cur).scale(sin))
                .add(axis.scale(axis.dot(cur) * (1 - cos)));
        setDeltaMovement(rotated);
    }

    // ==================== 模型渲染（按实体注册名自动解析，委托官方默认实现） ====================

    @Override
    public ResourceLocation getModel() {
        return BasicGeoProjectileEntity.DefaultImpls.getModel(this);
    }

    @Override
    public ResourceLocation getAnimation() {
        return BasicGeoProjectileEntity.DefaultImpls.getAnimation(this);
    }

    @Override
    public BasicProjectileAnimationInstance<?> getAnimationInstance() {
        return BasicGeoProjectileEntity.DefaultImpls.getAnimationInstance(this);
    }

    @Override
    public ResourceLocation getEmissiveTexture() {
        return BasicGeoProjectileEntity.DefaultImpls.getEmissiveTexture(this);
    }

    @Override
    public int getHiddenTicks() {
        return BasicGeoProjectileEntity.DefaultImpls.getHiddenTicks(this);
    }

    @Override
    public int getFlareHiddenTicks() {
        return BasicGeoProjectileEntity.DefaultImpls.getFlareHiddenTicks(this);
    }

    @Override
    public BakedModelInstance getModelInstance() {
        return BasicGeoProjectileEntity.DefaultImpls.getModelInstance(this);
    }
}
