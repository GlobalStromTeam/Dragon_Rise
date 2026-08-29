package com.redabysslucia.dragonrise_reforge.entities.projectile;

import com.atsuishio.superbwarfare.entity.projectile.BasicGeoProjectileEntity;
import com.atsuishio.superbwarfare.entity.projectile.WireGuideMissileEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class AntiTopWireGuideMissileEntity extends WireGuideMissileEntity implements BasicGeoProjectileEntity {

    public AntiTopWireGuideMissileEntity(EntityType<? extends AntiTopWireGuideMissileEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public Item getDefaultItem() {
        return ModItems.MEDIUM_ANTI_GROUND_MISSILE.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && this.tickCount > 5) {
            checkTopAttack();
        }
    }

    private void spawnTopAttackParticles(Vec3 pos) {
        ServerLevel serverLevel = (ServerLevel) level();
        for (int i = 0; i < 25; i++) {
            double theta = Math.random() * Math.toRadians(20);
            double phi = Math.random() * 2 * Math.PI;
            double dx = Math.sin(theta) * Math.cos(phi);
            double dy = Math.cos(theta);
            double dz = Math.sin(theta) * Math.sin(phi);
            double speed = 0.8 + Math.random() * 1.2;
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z, 0,
                    dx * speed, -dy * speed, dz * speed, 0.1);
        }
        for (int i = 0; i < 15; i++) {
            double theta = Math.random() * Math.toRadians(20);
            double phi = Math.random() * 2 * Math.PI;
            double dx = Math.sin(theta) * Math.cos(phi);
            double dy = Math.cos(theta);
            double dz = Math.sin(theta) * Math.sin(phi);
            double speed = 0.3 + Math.random() * 0.6;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x, pos.y, pos.z, 0,
                    dx * speed, -dy * speed * 0.5, dz * speed, 0.02);
        }
    }

    private float getDamageAmount() {
        try {
            Field field = com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile.class.getDeclaredField("damageValue");
            field.setAccessible(true);
            return field.getFloat(this);
        } catch (Exception e) {
            return 400f;
        }
    }

    private void checkTopAttack() {
        Vec3 pos = this.position();
        AABB searchBox = new AABB(
                pos.x - 2, pos.y - 2, pos.z - 2,
                pos.x + 2, pos.y, pos.z + 2
        );

        List<Entity> entities = level().getEntities(this, searchBox, entity -> entity instanceof VehicleEntity);

        UUID launcherUUID = super.getLauncherVehicleUUID();

        for (Entity entity : entities) {
            if (entity instanceof VehicleEntity vehicleEntity) {
                if (launcherUUID != null && launcherUUID.equals(entity.getUUID())) {
                    continue;
                }

                spawnTopAttackParticles(pos);

                float damage = getDamageAmount();
                Entity owner = this.getOwner();
                if (owner instanceof net.minecraft.world.entity.LivingEntity livingOwner) {
                    vehicleEntity.hurt(this.damageSources().mobProjectile(this, livingOwner), damage);
                } else {
                    vehicleEntity.hurt(this.damageSources().generic(), damage);
                }

                this.causeExplode(pos);
                this.discard();
                return;
            }
        }
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.ROCKET_FLY.get();
    }

    @Override
    public float getVolume() {
        return 0.4f;
    }

    @Override
    public float getMaxHealth() {
        return 20f;
    }

    @Override
    public ResourceLocation getModel() {
        return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "projectile/anti_top_wire_guide_missile");
    }

    @Override
    public ResourceLocation getAnimation() {
        return null;
    }

    @Override
    public com.atsuishio.superbwarfare.client.animation.entity.BasicProjectileAnimationInstance<?> getAnimationInstance() {
        return null;
    }

    @Override
    public ResourceLocation getEmissiveTexture() {
        return null;
    }

    @Override
    public int getHiddenTicks() {
        return 0;
    }

    @Override
    public int getFlareHiddenTicks() {
        return 3;
    }
}
