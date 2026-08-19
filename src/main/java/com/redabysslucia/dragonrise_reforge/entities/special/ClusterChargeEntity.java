package com.redabysslucia.dragonrise_reforge.entities.special;

import com.atsuishio.superbwarfare.config.server.ExplosionConfig;
import com.atsuishio.superbwarfare.entity.projectile.HandGrenadeEntity;
import com.atsuishio.superbwarfare.init.ModParticleTypes;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Optional;
import java.util.UUID;

/**
 * 仿《彩虹六号：围攻》FUZE 干员的"霰射炸药"（Cluster Charge）。
 * 放置方式参照 superbwarfare 的 EDD（Entry Denial Device）：可贴在方块侧面或地板上（不能贴天花板）。
 * 放置 0.8 秒后播放启动音效，同时出弹口开始喷出 superbwarfare 的 fire_star 火花（持续到射击结束）；
 * 音效播完后在附着面"后方/下方"朝房间内连续发射 5 发 superbwarfare M67 手榴弹，每次间隔 0.5 秒；
 * 发射方向在从左到右的 60° 扇形内均匀分布，手榴弹引信 2 秒、爆炸范围为 M67 原始值的三分之一。
 */
public class ClusterChargeEntity extends HangingEntity implements OwnableEntity {

    // ---- 发射参数（均可按需调整）----
    /** 放置后到播放启动音效的延迟（ticks，0.8 秒 = 16 ticks） */
    public static final int PLACE_SOUND_DELAY_TICKS = 16;
    /** 启动音效时长（ticks，3 秒 = 60 ticks），音效播完后才开始发射 */
    public static final int ARMING_SOUND_TICKS = 60;
    /** 开始发射的时刻（ticks） */
    public static final int FIRING_START_TICK = PLACE_SOUND_DELAY_TICKS + ARMING_SOUND_TICKS;
    /** 发射次数 */
    public static final int SHOT_COUNT = 5;
    /** 发射间隔（ticks，0.5 秒 = 10 ticks） */
    public static final int SHOT_INTERVAL_TICKS = 10;
    /** 手榴弹初速（格/ tick） */
    public static final float GRENADE_VELOCITY = 0.87f;
    /** 手榴弹引信延迟（ticks，2 秒 = 40 ticks） */
    public static final int GRENADE_FUSE_TICKS = 40;
    /** 发射扇形总角度（度）：从左到右均匀分布 */
    public static final float FAN_ANGLE_DEGREES = 60.0f;
    /** 地板放置时发射点横向铺开半径（格）：手榴弹在楼下横向散开 */
    public static final double CONE_RADIUS = 5.0;

    /** 发射音效：superbwarfare M79 榴弹发射器开火（1P 音效） */
    private static final SoundEvent FIRE_SOUND =
            SoundEvent.createVariableRangeEvent(new ResourceLocation("superbwarfare", "m_79_fire_1p"));

    /** 启动音效：放置 0.2 秒后播放的 3 秒启动音（dragonrise_reforge:cluster_charge_starting） */
    private static final SoundEvent ARMING_SOUND =
            SoundEvent.createVariableRangeEvent(new ResourceLocation("dragonrise_reforge", "cluster_charge_starting"));

    /** 0 - 左上; 1 - 左下; 2 - 右下; 3 - 右上 */
    private int corner;
    /** 已发射次数 */
    private int shotsFired;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(ClusterChargeEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public ClusterChargeEntity(EntityType<? extends ClusterChargeEntity> type, Level level) {
        super(type, level);
    }

    public ClusterChargeEntity(EntityType<? extends ClusterChargeEntity> type, LivingEntity owner, Level level) {
        super(type, level);
        if (owner != null) {
            this.setOwnerUUID(owner.getUUID());
        }
    }

    public ClusterChargeEntity(EntityType<? extends ClusterChargeEntity> type, LivingEntity owner, Level level, BlockPos pos, Direction direction, int corner) {
        super(type, level, pos);
        this.corner = corner;
        if (owner != null) {
            this.setOwnerUUID(owner.getUUID());
        }
        this.setDirection(direction);
    }

    // ---- 便捷构造（内部自动取注册的实体类型，与 EDD 的 Kotlin 默认参数等价）----

    public ClusterChargeEntity(Level level) {
        this(ModEntities.CLUSTER_CHARGE.get(), level);
    }

    public ClusterChargeEntity(LivingEntity owner, Level level) {
        this(ModEntities.CLUSTER_CHARGE.get(), owner, level);
    }

    public ClusterChargeEntity(LivingEntity owner, Level level, BlockPos pos, Direction direction, int corner) {
        this(ModEntities.CLUSTER_CHARGE.get(), owner, level, pos, direction, corner);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ClusterChargeEntity) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID, Optional.empty());
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Corner", this.corner);
        tag.putInt("ShotsFired", this.shotsFired);
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }
        tag.putByte("Facing", (byte) this.direction.get3DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Corner")) {
            this.corner = tag.getInt("Corner");
        }
        if (tag.contains("ShotsFired")) {
            this.shotsFired = tag.getInt("ShotsFired");
        }

        UUID uuid = null;
        if (tag.hasUUID("Owner")) {
            uuid = tag.getUUID("Owner");
        } else if (tag.contains("Owner")) {
            try {
                uuid = UUID.fromString(tag.getString("Owner"));
            } catch (Exception ignored) {
            }
        }
        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable ignored) {
            }
        }

        if (tag.contains("Facing")) {
            this.setDirection(Direction.from3DDataValue(tag.getByte("Facing")));
        }
    }

    @Override
    protected void setDirection(Direction direction) {
        this.direction = direction;
        if (direction.getAxis().isHorizontal()) {
            this.setXRot(0.0f);
            this.setYRot((float) (direction.get2DDataValue() * 90));
        } else {
            this.setXRot((float) (-90 * direction.getAxisDirection().getStep()));
            this.setYRot(0.0f);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    public void recalculateBoundingBox() {
        Direction dir = this.direction;
        if (dir == null) return;

        double d0 = 0.46875;
        double centerX = this.pos.getX() + 0.5 - dir.getStepX() * d0;
        double centerY = this.pos.getY() + 0.5 - dir.getStepY() * d0;
        double centerZ = this.pos.getZ() + 0.5 - dir.getStepZ() * d0;

        double halfWidth = this.getWidth() / 32.0;
        double halfHeight = this.getHeight() / 32.0;

        Vec3 cornerOffset = calculateCornerOffset(dir, this.corner, halfWidth, halfHeight);

        double finalX = centerX + cornerOffset.x;
        double finalY = centerY + cornerOffset.y;
        double finalZ = centerZ + cornerOffset.z;

        this.setPosRaw(finalX, finalY, finalZ);

        double dx = this.getWidth() / 32.0;
        double dy = this.getHeight() / 32.0;
        double dz = this.getWidth() / 32.0;
        switch (dir.getAxis()) {
            case X -> dx = 1.0 / 32.0;
            case Y -> dy = 1.0 / 32.0;
            case Z -> dz = 1.0 / 32.0;
        }

        this.setBoundingBox(new AABB(
                finalX - dx, finalY - dy, finalZ - dz,
                finalX + dx, finalY + dy, finalZ + dz
        ));
    }

    private Vec3 calculateCornerOffset(Direction direction, int corner, double width, double height) {
        if (corner < 0 || corner > 3) return Vec3.ZERO;

        boolean left = (corner == 0 || corner == 1);   // 左
        boolean top = (corner == 0 || corner == 3);   // 上
        double signY = top ? 1.0 : -1.0;

        return switch (direction) {
            case NORTH -> {
                double signX = left ? 1.0 : -1.0;
                yield new Vec3(signX * width, signY * height, 0.0);
            }
            case SOUTH -> {
                double signX = left ? -1.0 : 1.0;
                yield new Vec3(signX * width, signY * height, 0.0);
            }
            case WEST -> {
                double signZ = left ? -1.0 : 1.0;
                yield new Vec3(0.0, signY * height, signZ * width);
            }
            case EAST -> {
                double signZ = left ? 1.0 : -1.0;
                yield new Vec3(0.0, signY * height, signZ * width);
            }
            default -> Vec3.ZERO;
        };
    }

    @Override
    public int getWidth() {
        return 8;
    }

    @Override
    public int getHeight() {
        return 8;
    }

    @Override
    public void dropItem(Entity pBrokenEntity) {
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1f, 1f);
    }

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        } else {
            var blockstate = this.level().getBlockState(this.pos.relative(this.direction.getOpposite()));
            boolean wallOk = blockstate.isSolid()
                    || (this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(blockstate));
            return wallOk && this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        int data = this.corner * 10 + this.direction.get3DDataValue();
        return new ClientboundAddEntityPacket(this, data, this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.corner = packet.getData() / 10;
        this.setDirection(Direction.from3DDataValue(packet.getData() % 10));
    }

    public void setOwnerUUID(UUID pUuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(pUuid));
    }

    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    /** 模型朝左（对应 EDD 的 corner 0/1），供渲染器决定是否镜像 */
    public boolean isFacingLeft() {
        return this.corner == 0 || this.corner == 1;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.isOwnedBy(player) && player.isShiftKeyDown()) {
            if (!this.level().isClientSide()) {
                this.discard();
            }
            if (!player.getAbilities().instabuild) {
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(com.redabysslucia.dragonrise_reforge.init.ModItems.CLUSTER_CHARGE.get()));
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        // 放置 0.8 秒后播放启动音效
        if (this.tickCount == PLACE_SOUND_DELAY_TICKS) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ARMING_SOUND, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        // 0.8 秒后出弹口持续喷出 fire_star 火花，直到射击结束
        if (this.tickCount >= PLACE_SOUND_DELAY_TICKS) {
            this.spawnPortSparks();
        }

        // 启动音效播放结束后开始发射：第 1 发在 FIRING_START_TICK，之后每 SHOT_INTERVAL_TICKS 一发
        if (this.tickCount >= FIRING_START_TICK
                && this.shotsFired < SHOT_COUNT
                && (this.tickCount - FIRING_START_TICK) % SHOT_INTERVAL_TICKS == 0) {
            this.fireGrenade(this.shotsFired);
            this.shotsFired++;
        }

        if (this.shotsFired >= SHOT_COUNT) {
            this.discard();
        }
    }

    /**
     * 实体被移除（被打掉/潜行回收/自毁）时，若启动音效仍在播放，向玩家发送停止音效数据包，
     * 避免音效脱离实体继续播放。
     */
    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide()
                && this.tickCount >= PLACE_SOUND_DELAY_TICKS
                && this.tickCount <= FIRING_START_TICK) {
            ClientboundStopSoundPacket stopPacket =
                    new ClientboundStopSoundPacket(ARMING_SOUND.getLocation(), SoundSource.BLOCKS);
            for (Player player : this.level().players()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(stopPacket);
                }
            }
        }
        super.remove(reason);
    }

    /**
     * 出弹口火花：在设备前端（贴近墙面处）持续喷出 superbwarfare 的 fire_star 粒子，
     * 火花方向朝墙内（与手榴弹穿墙方向一致）。
     */
    private void spawnPortSparks() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 launchDir = Vec3.atLowerCornerOf(this.direction.getOpposite().getNormal());
        Vec3 port = Vec3.atCenterOf(this.pos).add(launchDir.scale(0.45));

        serverLevel.sendParticles(ModParticleTypes.FIRE_STAR.get(),
                port.x, port.y, port.z, 2,
                launchDir.x * 0.35, launchDir.y * 0.35, launchDir.z * 0.35, 0.35);
    }

    /**
     * 在附着面的"后方/下方"发射一发 superbwarfare M67 手榴弹。
     * 附着面法线为 this.direction（墙为水平面、地板为 UP），设备位于附着面外侧；
     * 发射点取附着面背后一格的方块中心，方向沿 -direction 穿过附着面（R6S FUZE 的行为），
     * 并按发射序号在从左到右的 FAN_ANGLE_DEGREES 扇形内均匀分布（无随机散布）；
     * 仅地板（朝下）放置时，发射位置横向铺开 CONE_RADIUS，让手榴弹在楼下横向散开。
     * 手榴弹引信为 GRENADE_FUSE_TICKS（4 秒），期间可弹跳。
     */
    private void fireGrenade(int shotIndex) {
        if (this.level().isClientSide()) return;

        Direction launchDir = this.direction.getOpposite();
        Vec3 baseDir = Vec3.atLowerCornerOf(launchDir.getNormal());

        // 扇形均匀分布：第 shotIndex 发（0..SHOT_COUNT-1）偏角 = -30° .. +30°
        float angle = -FAN_ANGLE_DEGREES / 2f
                + (FAN_ANGLE_DEGREES / (float) (SHOT_COUNT - 1)) * shotIndex;
        Vec3 dir = baseDir.yRot((float) Math.toRadians(angle));

        // 附着面背后一格（this.pos 为设备所在空气格，墙 = pos.relative(-direction)，背后 = pos.relative(-direction, 2)）
        BlockPos behind = this.pos.relative(launchDir, 2);
        Vec3 spawn = Vec3.atCenterOf(behind).add(dir.scale(0.25));

        // 仅地板放置：发射点横向铺开（圆锥扩散），手榴弹从不同位置垂直穿板
        if (this.direction == Direction.UP) {
            Vec3 up = new Vec3(0.0, 1.0, 0.0);
            Vec3 side = baseDir.cross(up);
            if (side.lengthSqr() < 1.0e-6) {
                side = new Vec3(1.0, 0.0, 0.0);
            }
            side = side.normalize();
            double rad = Math.toRadians(angle);
            spawn = spawn.add(side.scale(CONE_RADIUS * Math.sin(rad)))
                    .add(baseDir.scale(CONE_RADIUS * (1.0 - Math.cos(rad))));
        }

        LivingEntity owner = this.getOwner();
        HandGrenadeEntity grenade = new HandGrenadeEntity(owner, this.level());
        grenade.setLife(GRENADE_FUSE_TICKS);
        // 爆炸范围缩减为 M67 原始配置值的三分之一
        grenade.setExplosionRadius(ExplosionConfig.M67_GRENADE_EXPLOSION_RADIUS.get().floatValue() / 3f);
        grenade.setPos(spawn.x, spawn.y, spawn.z);
        grenade.shoot(dir.x, dir.y, dir.z, GRENADE_VELOCITY, 0.0f);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                FIRE_SOUND, SoundSource.BLOCKS, 1.0f, 1.0f);
        this.level().addFreshEntity(grenade);
    }
}
