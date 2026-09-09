package com.redabysslucia.dragonrise_reforge.entities.special;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 信号枪照明弹（Star Shell）：右键击发后上抛飞行，速度耗尽/撞到东西即在高空（或地面）悬停，
 * 悬停期间作为「超级大范围动态光源」持续照明（默认 72 格、45 秒，见 LIGHT_RADIUS/BURN_TICKS）。
 *
 * 补光机制（纯客户端，见 mixin/DynamicLights*.java）：借 dynamiclights-reforged 管线，
 * 在 SodiumDynamicLights.maxDynamicLightLevel 回调里对本实体返回 72 格全向平滑衰减亮度，
 * 抬高周围方块 lightmap —— 与「盈月」同源思路；但照明弹是全向且悬停不动，
 * 光值随区块烘焙一次后静止，不逐帧重排，性能开销可控。
 *
 * 服务端只负责：实体运动/悬停/存活计时/少量轨迹粒子。光源本身无需任何服务端/网络逻辑。
 */
public class StarShellEntity extends Entity {

    /** 全向照明半径（格）。客户端补光 mixin 读取同一常量保持公式一致。 */
    public static final double LIGHT_RADIUS = 72.0;

    /** 悬停燃烧时长：45 秒 */
    public static final int BURN_TICKS = 45 * 20;

    private static final EntityDataAccessor<Boolean> HOVERING =
            SynchedEntityData.defineId(StarShellEntity.class, EntityDataSerializers.BOOLEAN);

    /** 已悬停燃烧的 tick（服务端） */
    private int lifeTicks;

    public StarShellEntity(EntityType<? extends StarShellEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HOVERING, false);
    }

    public void setHovering(boolean hovering) {
        this.entityData.set(HOVERING, hovering);
    }

    public boolean isHovering() {
        return this.entityData.get(HOVERING);
    }

    /** 剩余燃烧 tick（客户端淡出动画用） */
    public int getRemainingBurnTicks() {
        return this.level().isClientSide() ? -1 : BURN_TICKS - this.lifeTicks;
    }

    /** 发射：沿视线方向射出（看向哪飞向哪），速度 1.7 左右 */
    public void launch(Vec3 look, float power) {
        Vec3 vel = look.normalize().scale(1.2 + 0.5 * power);
        this.setDeltaMovement(vel);
        this.hasImpulse = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();

        if (!this.isHovering()) {
            // ---- 飞行阶段：沿视线抛体，轻重力+空气阻尼 ----
            Vec3 vel = this.getDeltaMovement();
            vel = vel.add(0.0, -0.03, 0.0);
            this.setDeltaMovement(vel);
            this.move(MoverType.SELF, vel);
            this.setDeltaMovement(vel.scale(0.995));

            // 拖尾火花（每 tick，超视距可见：逐玩家 pLongDistance=true → 512 格门限，
            // 客户端收到 force 包无条件生成粒子，跳过 1024 距离判定）
            if (this.tickCount % 2 == 0) {
                for (ServerPlayer viewer : serverLevel.players()) {
                    serverLevel.sendParticles(viewer, ParticleTypes.FIREWORK, true,
                            this.getX(), this.getY(), this.getZ(),
                            2, 0.1, 0.1, 0.1, 0.02);
                }
            }

            // 悬停时机：着地/撞墙，或开始下落后再飞一小段（至少飞行 12 tick），或超时保险
            boolean hitWall = this.horizontalCollision || this.verticalCollision;
            boolean falling = this.getDeltaMovement().y <= 0.0 && this.tickCount > 12;
            if (this.onGround() || hitWall || falling || this.tickCount > 120) {
                this.startHover();
            }
            return;
        }

        // ---- 悬停照明阶段：燃烧计时，时间到才消失 ----
        if (++this.lifeTicks >= BURN_TICKS) {
            this.discard();
            return;
        }
        this.setDeltaMovement(Vec3.ZERO);

        // 悬停燃烧粒子：每 tick 向下落的火花 + 每 5 tick 一丝白烟（均超视距逐玩家 force 发送）
        for (ServerPlayer viewer : serverLevel.players()) {
            serverLevel.sendParticles(viewer, ParticleTypes.FIREWORK, true,
                    this.getX(), this.getY() - 0.2, this.getZ(),
                    1, 0.06, 0.15, 0.06, 0.01);
            if (this.lifeTicks % 5 == 0) {
                serverLevel.sendParticles(viewer, ParticleTypes.SMOKE, true,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.0, 0.3, 0.0, 0.0);
            }
        }
    }

    private void startHover() {
        this.setHovering(true);
        this.setDeltaMovement(Vec3.ZERO);
        // 撞地/贴墙悬停时把位置微微抬离表面，避免光球嵌进方块
        Vec3 pos = this.position();
        double nx = pos.x, ny = pos.y, nz = pos.z;
        if (this.onGround()) {
            ny = Math.floor(pos.y) + 0.6;
        }
        if (this.horizontalCollision) {
            nx = Math.floor(pos.x) + 0.5;
            nz = Math.floor(pos.z) + 0.5;
            ny = Math.max(ny, Math.floor(ny) + 0.4);
        }
        this.setPos(nx, ny, nz);
    }

    // ---- 渲染 / 通用 ----

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Hovering", this.isHovering());
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Hovering")) this.setHovering(tag.getBoolean("Hovering"));
        if (tag.contains("LifeTicks")) this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    public boolean isAttackable() {
        return true;
    }
}
