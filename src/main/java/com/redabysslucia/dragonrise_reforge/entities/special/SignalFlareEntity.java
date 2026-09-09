package com.redabysslucia.dragonrise_reforge.entities.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 信号棒 —— 仿 R6S / 战术信号棒：右键蓄力掷出，落地后喷射彩色烟雾 20 秒（NBT SmokeSeconds 可改）。
 * 烟雾粒子向上飘（CustomSmoke 由 SBW 客户端粒子实现，速度带向上分量）；
 * 与染料合成改变烟雾颜色（Color NBT）。
 * 蓄力投掷参照 SBW M18 烟雾弹的 use/releaseUsing 节奏。
 */
public class SignalFlareEntity extends Entity {

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(SignalFlareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SMOKE_SECONDS =
            SynchedEntityData.defineId(SignalFlareEntity.class, EntityDataSerializers.FLOAT);

    /** 已燃时间（tick） */
    private int burningTicks;
    /** 是否已落地 */
    private boolean landed;
    /** 落地时记录的水平速度，用于微微滑动后停住 */
    private int groundedTicks;

    public SignalFlareEntity(EntityType<? extends SignalFlareEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(COLOR, 0xFFFFFF); // 默认白烟（不染色）
        this.entityData.define(SMOKE_SECONDS, 20.0f); // 默认 20 秒
    }

    // ---- 属性 ----

    public void setSmokeColor(int rgb) {
        this.entityData.set(COLOR, rgb);
    }

    public void setSmokeSeconds(float seconds) {
        this.entityData.set(SMOKE_SECONDS, Math.max(1.0f, seconds));
    }

    public int getSmokeColor() {
        return this.entityData.get(COLOR);
    }

    public float getSmokeSeconds() {
        return this.entityData.get(SMOKE_SECONDS);
    }

    /** 是否已落地（渲染用：飞行中竖直，落地直立） */
    public boolean isLanded() {
        return this.landed;
    }

    /** 掷出：设置初速度（蓄力决定水平分量，竖直固定上抛） */
    public void launch(Vec3 direction, float power) {
        Vec3 vel = direction.scale(1.5f * power + 0.2f).add(0.0, 0.5 * power + 0.2, 0.0);
        this.setDeltaMovement(vel);
        this.hasImpulse = true;
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6f, 1.2f);
        }
    }

    // ---- tick ----

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        // ---- 飞行阶段（未落地）：抛物线 + 落地检测 ----
        if (!this.landed) {
            Vec3 vel = this.getDeltaMovement();
            vel = vel.add(0.0, -0.04, 0.0); // 轻重力（比手榴弹慢，便于飞远）
            this.setDeltaMovement(vel);
            this.move(MoverType.SELF, vel);
            this.setDeltaMovement(vel.scale(0.98)); // 空气阻力

            if (this.onGround()) {
                this.landed = true;
                this.groundedTicks = 0;
                // 落地即开始喷烟
                this.burningTicks = 0;
            }
            // 超过 5 秒仍未落地则直接落地（防卡天花/水里无限飘）
            if (this.tickCount > 100) {
                this.landed = true;
                this.groundedTicks = 0;
                this.burningTicks = 0;
            }
            return;
        }

        // ---- 喷烟阶段 ----
        int maxBurningTicks = (int) (this.getSmokeSeconds() * 20.0f);
        if (this.burningTicks >= maxBurningTicks) {
            this.discard();
            return;
        }
        this.burningTicks++;

        // 落地微微停稳（1 tick 后不再移动）
        if (this.groundedTicks < 1) {
            this.groundedTicks++;
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }

        // 每 tick 喷自定义彩色上升烟粒子（SignalSmokeParticle：POOF 贴图 + 染料颜色，
        // 向上飘、少量横向扩散）。颜色来自 NBT Color（默认白色 0xFFFFFF）。
        //
        // 距离机制（已对照 1.20.1 原版源码确认，见 sbw_research/vanilla_src）：
        //  * 服务端：9 参 sendParticles 内部对每个玩家做 closerToCenterThan(32) 判定，32 格外
        //    根本不会发包——这就是"32 格外看不见"的真正原因；
        //  * 客户端：收到带 force 标志的 ClientboundLevelParticlesPacket 后无条件生成粒子，
        //    跳过 LevelRenderer#addParticleInternal 的 1024 距离判定（我们的信号烟类型本就
        //    overrideLimiter=true，双重保险）；
        //  * 渲染端：ParticleEngine 只按视锥剔除，无距离剔除。
        // 因此改为对每个玩家调用带 pLongDistance=true 的逐玩家重载，服务端门限升到 512 格，
        // 已超过任何玩家的渲染距离+雾，等效"想多远就多远"。带宽：每 tick 每玩家 4 个
        // 几十字节的小包，可忽略。
        int color = this.getSmokeColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        int puffs = 4; // 恒定烟浓度（发光功能已移除，只保留染色）

        for (ServerPlayer viewer : serverLevel.players()) {
            for (int i = 0; i < puffs; i++) {
                double ox = (this.random.nextDouble() - 0.5) * 0.15;
                double oz = (this.random.nextDouble() - 0.5) * 0.15;
                double y = this.getY() + 0.3 + (this.random.nextDouble()) * 0.4;
                serverLevel.sendParticles(
                        viewer,
                        new com.redabysslucia.dragonrise_reforge.client.particle.SignalSmokeOption(r, g, b),
                        true, // pLongDistance：门限 32 → 512
                        this.getX() + ox, y, this.getZ() + oz,
                        1,
                        0.0,
                        0.3 + this.random.nextDouble() * 0.2, // 上飘速度
                        0.0,
                        0.0);
            }
        }
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
        tag.putInt("Color", this.getSmokeColor());
        tag.putFloat("SmokeSeconds", this.getSmokeSeconds());
        tag.putBoolean("Landed", this.landed);
        tag.putInt("BurningTicks", this.burningTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Color")) this.entityData.set(COLOR, tag.getInt("Color"));
        if (tag.contains("SmokeSeconds")) this.entityData.set(SMOKE_SECONDS, tag.getFloat("SmokeSeconds"));
        if (tag.contains("Landed")) this.landed = tag.getBoolean("Landed");
        if (tag.contains("BurningTicks")) this.burningTicks = tag.getInt("BurningTicks");
    }

    @Override
    public boolean isAttackable() {
        return true;
    }
}
