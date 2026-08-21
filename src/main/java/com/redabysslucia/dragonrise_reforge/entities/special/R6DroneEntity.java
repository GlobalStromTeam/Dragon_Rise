package com.redabysslucia.dragonrise_reforge.entities.special;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.UUID;

/**
 * 仿《彩虹六号：围攻》Twitch/侦察无人车 —— 以原版实体（继承 Entity）形式实现。
 * 不继承 superbwarfare 的 DroneEntity/VehicleEntity，绕开其载具数据与输入/相机同步体系。
 *
 * 控制：superbwarfare 的 monitor（监控平板）
 * - 右键无人车链接，右键平板激活遥控（视角绑定由本项目自研的 R6DroneCameraMixin 实现，参考 SBW 的 CameraMixin）
 * - WASD 前后左右移动（以视角方向为基准）；Ctrl 冲刺（速度 ×4/3）
 * - 空格朝视角方向跳跃：平视小跳/前冲，视角抬到最高跳约 4~5 格，冷却 2 秒
 * - 移动/跳跃/落地播放 drone_moving / drone_jump / drone_down
 *
 * 朝向逻辑：服务器每 tick 直接读取控制者玩家的旋转（玩家旋转天然同步到服务器），
 * 所有客户端看到的转向一致且即时；相机/车身在控制端直接使用玩家视角（原生手感，无平滑）。
 */
public class R6DroneEntity extends Entity {

    // ---- 移动 ----
    public static final double BASE_SPEED = 0.32;
    public static final double SPRINT_MULTIPLIER = 4.0 / 3.0;
    public static final double GRAVITY = 0.08;
    public static final double ACCEL_GROUND = 0.25;
    public static final double ACCEL_AIR = 0.08;

    // ---- 跳跃 ----
    public static final double JUMP_SPEED = 0.62;
    public static final double JUMP_MIN_VY = 0.25;
    public static final double JUMP_HORIZONTAL_SPEED = 0.6;
    public static final int JUMP_COOLDOWN = 40;

    // ---- 音效 ----
    // 音效必须在 ForgeRegistries.SOUND_EVENTS 注册（ModSounds），否则 Level.playSound /
    // ClientboundSoundPacket 拿不到 Holder 会静默失败，远处听不到。

    /**
     * 播放无人车音效（仅服务端调用）。
     * 音源固定在无人车位置；遥控时直接把 ClientboundSoundPacket 发给控制者玩家
     * （客户端监听 = 主相机 = 无人机视角，距离 0 → 全音量），
     * 避免服务端 PlayerList.broadcast 按玩家实体位置过滤导致控制者收不到包。
     */
    private void playDroneSound(RegistryObject<SoundEvent> sound, float volume) {
        if (this.level().isClientSide()) return;
        Player controller = this.getController();
        if (controller instanceof ServerPlayer sp && this.isMonitorControlling(controller)) {
            sound.getHolder().ifPresent(holder ->
                    sp.connection.send(new ClientboundSoundPacket(
                            holder,
                            SoundSource.PLAYERS,
                            this.getX(), this.getY(), this.getZ(),
                            volume, 1.0f, this.level().getRandom().nextLong())));
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    sound.get(), SoundSource.PLAYERS, volume, 1.0f);
        }
    }

    private static final EntityDataAccessor<Optional<UUID>> CONTROLLER =
            SynchedEntityData.defineId(R6DroneEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // 输入状态（服务器，来自 R6DroneControlMessage）
    private boolean forward;
    private boolean back;
    private boolean left;
    private boolean right;
    private boolean sprint;
    private boolean jump;

    private float health = 40.0f;
    private int jumpCooldown;
    private boolean wasOnGround;

    public R6DroneEntity(EntityType<? extends R6DroneEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CONTROLLER, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // 客户端：位置插值完全交给原版机制。
            // ClientLevel.tickNonPassenger 每 tick 先调用 setOldPosAndRot()（xOld = 本 tick 开始位置），
            // 位置包（lerpTo→setPos）在 Connection.tick 阶段到达 —— 渲染时
            // LevelRenderer 用 lerp(partialTick, xOld, getX()) 平滑覆盖一个 tick 的位移，
            // 相机 mixin 的 getRenderPosition 用同一公式，两者严格同步。
            return;
        }

        Player controller = this.getController();
        boolean monitoring = controller != null && this.isMonitorControlling(controller);

        // 朝向 = 控制者玩家视角（服务器直接读取，即时、所有客户端一致）
        if (monitoring) {
            this.setYRot(controller.getYRot());
            this.setXRot(Mth.clamp(controller.getXRot(), -90.0f, 90.0f));
        } else {
            // 未在遥控：清空输入，避免无人车继续行驶
            this.forward = this.back = this.left = this.right = this.sprint = this.jump = false;
        }

        // 落地音效
        if (!this.wasOnGround && this.onGround()) {
            this.playDroneSound(ModSounds.R6_DRONE_DOWN, 1.0f);
        }
        this.wasOnGround = this.onGround();

        // 移动音效（间隔循环）
        boolean moving = (this.forward || this.back || this.left || this.right || this.sprint) && this.onGround();
        if (moving && this.tickCount % 10 == 0) {
            this.playDroneSound(ModSounds.R6_DRONE_MOVING, 0.4f);
        }

        if (this.jumpCooldown > 0) {
            this.jumpCooldown--;
        }

        // ---- 物理 ----
        Vec3 vel = this.getDeltaMovement();

        // 重力
        vel = vel.add(0.0, -GRAVITY, 0.0);

        // WASD 输入 → 期望水平速度（以视角方向为基准）
        double fwd = (this.forward ? 1.0 : 0.0) - (this.back ? 1.0 : 0.0);
        double strafe = (this.right ? 1.0 : 0.0) - (this.left ? 1.0 : 0.0);
        double yaw = Math.toRadians(this.getYRot());
        Vec3 forwardVec = new Vec3(-Mth.sin((float) yaw), 0.0, Mth.cos((float) yaw));
        Vec3 rightVec = new Vec3(-forwardVec.z, 0.0, forwardVec.x);
        Vec3 wish = forwardVec.scale(fwd).add(rightVec.scale(strafe));
        if (wish.lengthSqr() > 1.0e-6) {
            wish = wish.normalize();
        }

        double speed = BASE_SPEED * (this.sprint ? SPRINT_MULTIPLIER : 1.0);
        Vec3 target = wish.scale(speed);
        Vec3 horiz = new Vec3(vel.x, 0.0, vel.z);
        Vec3 newHoriz = horiz.lerp(target, this.onGround() ? ACCEL_GROUND : ACCEL_AIR);
        vel = new Vec3(newHoriz.x, vel.y, newHoriz.z);

        // 跳跃：朝视角方向，2 秒冷却
        if (this.jumpCooldown <= 0 && this.onGround() && this.jump) {
            Vec3 look = this.getLookAngle();
            double vx = look.x * JUMP_HORIZONTAL_SPEED;
            double vy = look.y * JUMP_SPEED + JUMP_MIN_VY;
            double vz = look.z * JUMP_HORIZONTAL_SPEED;
            vel = new Vec3(vx, vy, vz);
            this.jumpCooldown = JUMP_COOLDOWN;
            this.playDroneSound(ModSounds.R6_DRONE_JUMP, 1.0f);
        }

        this.setDeltaMovement(vel);
        this.move(MoverType.SELF, vel);
    }

    /** 服务器接收键盘输入（来自 R6DroneControlMessage） */
    public void processInput(short keys) {
        this.left = (keys & 0b000000001) > 0;
        this.right = (keys & 0b000000010) > 0;
        this.forward = (keys & 0b000000100) > 0;
        this.back = (keys & 0b000001000) > 0;
        this.jump = (keys & 0b000010000) > 0;
        this.sprint = (keys & 0b100000000) > 0;
    }

    // ---- 链接 / 交互 ----

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModItems.MONITOR.get())) {
            if (!player.isShiftKeyDown()) {
                if (this.getController() == null) {
                    this.entityData.set(CONTROLLER, Optional.of(player.getUUID()));
                    MonitorItem.link(stack, this.getStringUUID());
                    player.displayClientMessage(
                            Component.translatable("tips.superbwarfare.monitor.linked").withStyle(ChatFormatting.GREEN), true);
                    if (!this.level().isClientSide()) {
                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.5f, 1.0f);
                    }
                } else {
                    player.displayClientMessage(
                            Component.translatable("tips.superbwarfare.drone.already_linked").withStyle(ChatFormatting.RED), true);
                }
            } else {
                if (this.getController() != null) {
                    this.entityData.set(CONTROLLER, Optional.empty());
                    MonitorItem.disLink(stack, player);
                    player.displayClientMessage(
                            Component.translatable("tips.superbwarfare.monitor.unlinked").withStyle(ChatFormatting.RED), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    // ---- 伤害 / 销毁 ----

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved()) return false;
        this.health -= amount;
        if (this.health <= 0) {
            this.destroy();
        }
        return true;
    }

    private void destroy() {
        if (this.level().isClientSide()) return;
        Player controller = this.getController();
        if (controller != null) {
            ItemStack stack = controller.getMainHandItem();
            if (stack.is(ModItems.MONITOR.get())) {
                MonitorItem.disLink(stack, controller);
            }
        }
        this.level().explode(null, this.getX(), this.getY(), this.getZ(),
                0.8f, Level.ExplosionInteraction.NONE);
        this.discard();
    }

    // ---- 相机/渲染辅助（控制端直接用玩家视角，即时无平滑）----

    /** 控制端渲染车身时使用的偏航 */
    public float getRenderYaw(float partialTick) {
        if (this.level().isClientSide()) {
            Player local = Minecraft.getInstance().player;
            if (local != null && this.getController() == local && this.isMonitorControlling(local)) {
                return local.getYRot();
            }
        }
        return Mth.lerp(partialTick, this.yRotO, this.getYRot());
    }

    /** 控制端渲染车身时使用的俯仰 */
    public float getRenderPitch(float partialTick) {
        if (this.level().isClientSide()) {
            Player local = Minecraft.getInstance().player;
            if (local != null && this.getController() == local && this.isMonitorControlling(local)) {
                return local.getXRot();
            }
        }
        return Mth.lerp(partialTick, this.xRotO, this.getXRot());
    }

    // ---- 工具 ----

    // 客户端查找缓存：findDrone 在渲染线程每帧被相机 mixin 调用，
    // entitiesForRendering() 遍历全部实体是 O(N)，场景实体多时会造成掉帧卡顿。
    // 缓存有效引用（跨 tick 复用），实体消失/换图/换 UUID 时自动失效重查。
    private static R6DroneEntity cachedClientDrone;
    private static String cachedClientDroneUuid;
    private static long cachedClientDroneLevelTick = -1;

    /** 按监控平板 LinkedDrone 的 UUID 查找无人车（服务端/客户端通用） */
    public static R6DroneEntity findDrone(Level level, String uuidString) {
        if (uuidString == null || uuidString.length() != 36) return null;
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (level.isClientSide()) {
            // 缓存命中：引用仍有效（同图、未移除、UUID 一致）则直接复用，避免每帧 O(N) 遍历
            if (cachedClientDrone != null && !cachedClientDrone.isRemoved()
                    && cachedClientDrone.level() == level
                    && cachedClientDroneUuid != null && cachedClientDroneUuid.equals(uuidString)) {
                return cachedClientDrone;
            }
            // 缓存失效：本 tick 已查过则不再重复遍历（结果为空说明确实不存在，保持 null）
            long tick = level.getGameTime();
            if (cachedClientDroneLevelTick == tick && cachedClientDroneUuid != null
                    && cachedClientDroneUuid.equals(uuidString)) {
                return cachedClientDrone;
            }
            cachedClientDroneLevelTick = tick;
            cachedClientDroneUuid = uuidString;
            cachedClientDrone = null;
            for (Entity ent : ((net.minecraft.client.multiplayer.ClientLevel) level).entitiesForRendering()) {
                if (ent.getUUID().equals(uuid) && ent instanceof R6DroneEntity drone) {
                    cachedClientDrone = drone;
                    break;
                }
            }
            return cachedClientDrone;
        }

        Entity e = ((net.minecraft.server.level.ServerLevel) level).getEntities().get(uuid);
        return e instanceof R6DroneEntity drone ? drone : null;
    }

    public Player getController() {
        UUID uuid = this.entityData.get(CONTROLLER).orElse(null);
        if (uuid == null) return null;
        return this.level().getPlayerByUUID(uuid);
    }

    /** 玩家是否正手持监控平板激活遥控本无人车 */
    public boolean isMonitorControlling(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.is(ModItems.MONITOR.get())
                && stack.getOrCreateTag().getBoolean(MonitorItem.USING)
                && stack.getOrCreateTag().getBoolean(MonitorItem.LINKED);
    }

    /** 渲染插值位置（相机/车身使用）。
     * 用原版 xOld/yOld/zOld（ClientLevel 每 tick 自动维护为本 tick 开始位置），
     * 与 LevelRenderer 车体渲染的 lerp(partialTick, xOld, getX()) 完全一致。 */
    public Vec3 getRenderPosition(float partialTick) {
        if (!this.level().isClientSide()) {
            return this.position();
        }
        return new Vec3(
                Mth.lerp(partialTick, this.xOld, this.getX()),
                Mth.lerp(partialTick, this.yOld, this.getY()),
                Mth.lerp(partialTick, this.zOld, this.getZ()));
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ---- 存档 ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", this.health);
        UUID uuid = this.entityData.get(CONTROLLER).orElse(null);
        if (uuid != null) {
            tag.putUUID("Controller", uuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Health")) {
            this.health = tag.getFloat("Health");
        }
        if (tag.hasUUID("Controller")) {
            this.entityData.set(CONTROLLER, Optional.of(tag.getUUID("Controller")));
        }
    }
}
