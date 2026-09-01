package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.Mod;
import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.atsuishio.superbwarfare.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;

public class TerroristEntity extends Monster implements RangedAttackMob, GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
    }

    public static final EntityDataAccessor<Boolean> RUNNER = SynchedEntityData.defineId(TerroristEntity.class, EntityDataSerializers.BOOLEAN);
    private int burstShotsRemaining = 0;
    private int burstTickCounter = 0;
    private int nextBurstTick = 0;
    private int shootAnimationTick = 0;

    public TerroristEntity(EntityType<TerroristEntity> type, Level world) {
        super(type, world);
        xpReward = 40;
        setNoAi(false);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RUNNER, false);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.entityData.set(RUNNER, Math.random() < 0.3);

        if (entityData.get(RUNNER)) {
            var attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.addPermanentModifier(new AttributeModifier(Mod.ATTRIBUTE_MODIFIER, 0.4, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        } else {
            var attribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attribute != null) {
                attribute.addPermanentModifier(new AttributeModifier(Mod.ATTRIBUTE_MODIFIER, 3, AttributeModifier.Operation.ADDITION));
            }
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Runner", this.entityData.get(RUNNER));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(RUNNER, compound.getBoolean("Runner"));
    }

    @Override
    @ParametersAreNonnullByDefault
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return 1.75F;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new RangedBowAttackGoal<TerroristEntity>(this, 1.0, 30, 35.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && !TerroristEntity.this.entityData.get(RUNNER) && TerroristEntity.this.nextBurstTick == 0;
            }
        });
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && TerroristEntity.this.entityData.get(RUNNER);
            }
            
            @Override
            protected double getAttackReachSqr(@NotNull LivingEntity entity) {
                return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
            }
        });
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new FloatGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.ILLAGER;
    }

    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);

        double random = Math.random();
        if (random < 0.01) {
            this.spawnAtLocation(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        } else if (random < 0.2) {
            this.spawnAtLocation(new ItemStack(Items.GOLDEN_APPLE));
        } else {
            this.spawnAtLocation(new ItemStack(Items.APPLE));
        }
    }

    @Override
    public SoundEvent getAmbientSound() {
        return ModSounds.TERRORIST_IDLE.get();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(ModSounds.TERRORIST_STEP.get(), 0.25f, 1);
    }

    @Override
    public @NotNull SoundEvent getHurtSound(@NotNull DamageSource ds) {
        return ModSounds.TERRORIST_HURT.get();
    }

    @Override
    public @NotNull SoundEvent getDeathSound() {
        return ModSounds.TERRORIST_DEATH.get();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        this.refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
        if (this.shootAnimationTick > 0) {
            this.shootAnimationTick--;
        }
        if (this.burstShotsRemaining > 0) {
            this.burstTickCounter--;
            if (this.burstTickCounter <= 0) {
                LivingEntity target = this.getTarget();
                if (target != null) {
                    this.fireProjectile(target);
                }
                this.burstShotsRemaining--;
                if (this.burstShotsRemaining > 0) {
                    this.burstTickCounter = 2;
                }
            }
        }
        if (this.nextBurstTick > 0) {
            this.nextBurstTick--;
        }
    }
    
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.fireProjectile(target);
        this.burstShotsRemaining = 3;
        this.burstTickCounter = 2;
        this.shootAnimationTick = 30;
        this.nextBurstTick = 20 + this.random.nextInt(20);
    }
    
    private void fireProjectile(LivingEntity target) {
        ProjectileEntity projectile = new ProjectileEntity(ModEntities.PROJECTILE.get(), this.level());
        projectile.setOwner(this);
        projectile.setRGB(new float[]{ProjectileEntity.DEFAULT_R, ProjectileEntity.DEFAULT_G, ProjectileEntity.DEFAULT_B});
        projectile.setDamage(4.0f);
        projectile.setCustomGravity(0.0f);
        projectile.velocity(15.0F);
        projectile.setPos(this.getEyePosition());
        
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, 15.0F, 2F);
        this.playSound(ModSounds.TERRORIST_SHOOT.get(), 5.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(projectile);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 12)
                .add(Attributes.ATTACK_DAMAGE, 8)
                .add(Attributes.FOLLOW_RANGE, 64)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
    }
@Override
    public void die(@NotNull DamageSource source) {
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 140) {
            this.remove(TerroristEntity.RemovalReason.KILLED);
            this.dropExperience();
        }
    }
}
