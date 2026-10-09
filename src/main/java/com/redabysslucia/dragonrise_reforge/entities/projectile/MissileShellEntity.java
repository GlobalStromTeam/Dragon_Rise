package com.redabysslucia.dragonrise_reforge.entities.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 红箭8 发射后向后弹出的空发射筒（纯表现实体）。
 * <p>
 * 由 {@code HJ8Entity.vehicleShoot} 在炮管尾部生成，沿炮管反方向弹出、受重力下坠、3 秒（60 tick）后消失。
 * 不参与碰撞与伤害（{@code noPhysics = true}），模型与贴图见
 * {@code assets/dragonrise_reforge/geo/missileshell.geo.json} 与 {@code textures/entity/missileshell.png}。
 */
public class MissileShellEntity extends Entity implements GeoEntity {

    /** 存活时长：3 秒 */
    public static final int LIFETIME_TICKS = 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MissileShellEntity(EntityType<? extends MissileShellEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;      // 纯表现：穿过一切，不做碰撞
        this.setInvulnerable(true);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /**
     * 从炮管尾部向后弹出。
     *
     * @param position 生成位置（世界坐标）
     * @param velocity 初速度（炮管反方向 + 少量上抛）
     */
    public void eject(Vec3 position, Vec3 velocity) {
        this.setPos(position.x, position.y, position.z);
        this.setDeltaMovement(velocity);
        // 让模型一出生就朝向飞行方向
        Vec3 dir = velocity.normalize();
        this.setYRot((float) (net.minecraft.util.Mth.atan2(dir.x, dir.z) * (180F / (float) java.lang.Math.PI)));
        this.setXRot((float) (-net.minecraft.util.Mth.atan2(dir.y, dir.horizontalDistance()) * (180F / (float) java.lang.Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.tickCount > LIFETIME_TICKS) {
            this.discard();
            return;
        }

        // 重力 + 空气阻力；碰到地面就停住（noPhysics 下自己判断，避免陷进地里）
        Vec3 motion = this.getDeltaMovement();
        Vec3 next = new Vec3(motion.x * 0.985, motion.y - 0.045, motion.z * 0.985);
        if (next.y < 0 && !this.level().noCollision(this, this.getBoundingBox().move(next))) {
            next = new Vec3(next.x * 0.6, 0, next.z * 0.6);      // 落地：抹掉竖直速度并摩擦减速
        }
        this.setDeltaMovement(next);
        this.move(MoverType.SELF, next);

        // 缓慢翻滚（客户端由 tickCount 推导，见渲染器）
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}
