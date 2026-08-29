package com.redabysslucia.dragonrise_reforge.entities.special;

import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.NBTTool;
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
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

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
    // 普通移动 = 玩家走路速度（原版 MOVEMENT_SPEED 0.1 格/tick），
    // 冲刺 = 玩家跑步速度（走路 × 1.3 ≈ 0.13 格/tick）
    public static final double BASE_SPEED = 0.28;
    public static final double SPRINT_MULTIPLIER = 1.3;
    public static final double GRAVITY = 0.08;
    public static final double ACCEL_GROUND = 0.6;
    public static final double ACCEL_AIR = 0.05;

    // ---- 跳跃 ----
    public static final double JUMP_SPEED = 0.62;
    public static final double JUMP_MIN_VY = 0.25;
    public static final double JUMP_HORIZONTAL_SPEED = 0.6;
    public static final int JUMP_COOLDOWN = 40;

    // ---- 音效 ----
    // 移动循环音（moving/fast）由客户端 R6DroneLoopSoundInstance 播放（循环 SoundInstance，绑定实体）；
    // 这里只播放一次性音效（跳跃/落地）。音效必须在 ForgeRegistries.SOUND_EVENTS 注册（ModSounds）。

    /**
     * 播放无人车一次性音效（仅服务端调用）。
     * 音源固定在无人车位置；遥控时直接把 ClientboundSoundPacket 发给控制者玩家
     * （客户端监听 = 主相机 = 无人机视角，距离 0 → 全音量），
     * 避免服务端 PlayerList.broadcast 按玩家实体位置过滤导致控制者收不到包。
     */
    private void playDroneSound(DeferredHolder<SoundEvent, SoundEvent> sound, float volume) {
        if (this.level().isClientSide()) return;
        Player controller = this.getController();
        if (controller instanceof ServerPlayer sp && this.isMonitorControlling(controller)) {
            sp.connection.send(new ClientboundSoundPacket(
                    net.minecraft.core.Holder.direct(sound.get()),
                    SoundSource.PLAYERS,
                    this.getX(), this.getY(), this.getZ(),
                    volume, 1.0f, this.level().getRandom().nextLong()));
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    sound.get(), SoundSource.PLAYERS, volume, 1.0f);
        }
    }

    /**
     * 播放任意注册表音效（如 superbwarfare 的枪声），机制同上：遥控时发给控制者专用包。
     * 用 Holder.direct 直接包装 SoundEvent，不依赖注册表查询（getHolder 可能返回空导致无声）。
     */
    protected void playDroneSound(SoundEvent sound, float volume) {
        if (this.level().isClientSide()) return;
        Player controller = this.getController();
        if (controller instanceof ServerPlayer sp && this.isMonitorControlling(controller)) {
            sp.connection.send(new ClientboundSoundPacket(
                    net.minecraft.core.Holder.direct(sound),
                    SoundSource.PLAYERS,
                    this.getX(), this.getY(), this.getZ(),
                    volume, 1.0f, this.level().getRandom().nextLong()));
        } else {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    sound, SoundSource.PLAYERS, volume, 1.0f);
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

    private float health = 5.0f;
    private int jumpCooldown;
    private boolean wasOnGround;

    // ---- 客户端渲染位置历史（Catmull-Rom 样条）----
    // 位置包到达节奏不稳定（网络/服务器抖动）时，lerp(xOld, x) 线性插值的端点
    // 速度会突变 → 移动时看世界顿挫。用上两个 tick 的位置做三次样条插值，
    // 曲线速度连续，吸收包抖动；大位移（跳跃/落地/传送）回退线性，保持瞬态不闪。
    private double prevPrevX;
    private double prevPrevY;
    private double prevPrevZ;
    private double prevX;
    private double prevY;
    private double prevZ;

    // 相机低通滤波：样条仍可能残留包到达节奏/增量编码精度的微小速度波动，
    // 相机平移时被放大成轻微顿挫。advanceSmoothPosition 每帧（渲染帧）把平滑位置
    // 向样条目标指数逼近，输出绝对平滑；瞬态（大位移）直接到位。车体渲染器只读。
    private double smoothX;
    private double smoothY;
    private double smoothZ;
    private boolean smoothInit;

    public R6DroneEntity(EntityType<? extends R6DroneEntity> type, Level level) {
        super(type, level);
        // 原版 Entity.maxUpStep 默认 0.0f，move() 台阶逻辑要求 > 0 才生效。
        // 设为 0.6 让无人车可以爬上 0.5 格的半砖/台阶（1.21: maxUpStep 改为方法，需覆写）。
    }

    @Override
    public float maxUpStep() {
        return 0.6f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CONTROLLER, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // 客户端：记录渲染位置历史（本 tick 开始位置，位置包在 Connection.tick 阶段才 setPos）。
            // ClientLevel.tickNonPassenger 已在本 tick 前调用 setOldPosAndRot()（xOld = 本 tick 开始位置），
            // 渲染时 getX() 已被位置包更新 —— 样条在 [prev, cur] 段插值，与 LevelRenderer 车体渲染一致。
            this.prevPrevX = this.prevX;
            this.prevPrevY = this.prevY;
            this.prevPrevZ = this.prevZ;
            this.prevX = this.getX();
            this.prevY = this.getY();
            this.prevZ = this.getZ();
            // 退出遥控视角后 advanceSmoothPosition 不再被相机 mixin 推进，
            // 平滑位置会滞留在旧值（如半空）。此时置 smoothInit=false，
            // 让渲染器回退到样条位置（跟随实体真实位置），避免模型滞留。
            Player local = Minecraft.getInstance().player;
            if (local == null || !this.isMonitorControlling(local)) {
                this.smoothInit = false;
            }
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
                    CompoundTag tag = NBTTool.getTag(stack);
                    MonitorItem.link(tag, this.getStringUUID());
                    NBTTool.saveTag(stack, tag);
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
                // shift+右键：回收无人车（返还部署物品，解除链接，移除实体）
                this.recycle(player);
            }
        } else if (player.isShiftKeyDown()) {
            // 空手或任意物品 shift+右键也可回收
            this.recycle(player);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    /** 回收无人车：返还部署物品、解除链接、移除实体（仅服务端执行实际回收） */
    private void recycle(Player player) {
        if (this.level().isClientSide()) return;
        // 解除控制者链接（仅当本无人车确实是 monitor 当前链接的目标）
        Player controller = this.getController();
        if (controller != null) {
            ItemStack ctrlStack = controller.getMainHandItem();
            if (ctrlStack.is(ModItems.MONITOR.get())) {
                String linked = NBTTool.getTag(ctrlStack).getString(MonitorItem.LINKED_DRONE);
                if (linked.equals(this.getStringUUID())) {
                    CompoundTag tag = NBTTool.getTag(ctrlStack);
                    MonitorItem.disLink(tag, controller);
                    NBTTool.saveTag(ctrlStack, tag);
                }
            }
        }
        // 返还对应部署物品（子类 override getDeployItem）
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(this.getDeployItem()));
        player.displayClientMessage(
                Component.translatable("tips.superbwarfare.drone.unlinked").withStyle(ChatFormatting.GREEN), true);
        this.discard();
    }

    /** 本无人车对应的部署物品（子类返回各自的物品） */
    protected net.minecraft.world.item.Item getDeployItem() {
        return com.redabysslucia.dragonrise_reforge.init.ModItems.R6_DRONE.get();
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
        if (this.level().isClientSide() || this.isRemoved()) return;
        Player controller = this.getController();
        if (controller != null) {
            ItemStack stack = controller.getMainHandItem();
            if (stack.is(ModItems.MONITOR.get())) {
                // 仅当 monitor 当前链接的确实是本无人车时才解除，
                // 否则场景中有多辆无人车时打掉任意一辆会误断当前遥控的链接。
                String linked = NBTTool.getTag(stack).getString(MonitorItem.LINKED_DRONE);
                if (linked.equals(this.getStringUUID())) {
                    CompoundTag tag = NBTTool.getTag(stack);
                    MonitorItem.disLink(tag, controller);
                    NBTTool.saveTag(stack, tag);
                }
            }
        }
        // 先 discard 再爆炸：爆炸是同步执行的，会立即伤害范围内实体（包括本车）。
        // 若先 explode，爆炸伤害会再触发 hurt()→destroy()→explode() 无限递归 → 栈溢出。
        this.discard();
        this.level().explode(null, this.getX(), this.getY(), this.getZ(),
                0.8f, Level.ExplosionInteraction.NONE);
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
                && NBTTool.getTag(stack).getBoolean(MonitorItem.USING)
                && NBTTool.getTag(stack).getBoolean(MonitorItem.LINKED);
    }

    /** 渲染插值位置（相机/车身使用）。
     * 用 Catmull-Rom 样条在 [prev, cur] 段插值：曲线平滑穿过路径点、速度连续，
     * 吸收位置包到达节奏抖动（线性 lerp 的端点速度突变会造成移动顿挫）。
     * 大位移（跳跃/落地/传送，prev→cur 超过 MAX_SPLINE_STEP）回退线性插值，避免样条过冲。
     * 与 LevelRenderer 车体渲染的 lerp(partialTick, xOld, getX()) 语义一致（同区间），
     * 相机与车体严格同步。 */
    private static final double MAX_SPLINE_STEP = 0.9;
    /** 相机低通滤波系数（每渲染帧向目标逼近的比例） */
    private static final double SMOOTH_ALPHA = 0.25;

    public Vec3 getRenderPosition(float partialTick) {
        if (!this.level().isClientSide()) {
            return this.position();
        }
        double cx = this.getX(), cy = this.getY(), cz = this.getZ();
        double dx = cx - this.prevX, dy = cy - this.prevY, dz = cz - this.prevZ;
        double stepSqr = dx * dx + dy * dy + dz * dz;
        if (stepSqr > MAX_SPLINE_STEP * MAX_SPLINE_STEP) {
            // 瞬态（跳跃/落地/传送）：线性插值，等效原版 lerp(xOld, x)
            return new Vec3(
                    Mth.lerp(partialTick, this.xOld, cx),
                    Mth.lerp(partialTick, this.yOld, cy),
                    Mth.lerp(partialTick, this.zOld, cz));
        }
        return new Vec3(
                catmullRom(partialTick, this.prevPrevX, this.prevX, cx),
                catmullRom(partialTick, this.prevPrevY, this.prevY, cy),
                catmullRom(partialTick, this.prevPrevZ, this.prevZ, cz));
    }

    /** Catmull-Rom 三次样条：在 [p1, p2] 段插值，p0/p3 提供切线（p3 用 p2+(p2-p1) 外推） */
    private static double catmullRom(float t, double p0, double p1, double p2) {
        double p3 = p2 + (p2 - p1);
        double t2 = t * t, t3 = t2 * t;
        return 0.5 * (
                (2.0 * p1)
                        + (-p0 + p2) * t
                        + (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2
                        + (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3);
    }

    /** 相机/车体统一使用：每渲染帧调用一次（Camera.setup），把平滑位置向样条目标指数逼近。
     * 瞬态（大位移，样条回退线性）时直接到位，避免起跳/落地被钝化。 */
    public Vec3 advanceSmoothPosition(float partialTick) {
        Vec3 target = this.getRenderPosition(partialTick);
        if (!this.smoothInit) {
            this.smoothX = target.x;
            this.smoothY = target.y;
            this.smoothZ = target.z;
            this.smoothInit = true;
            return target;
        }
        // 大位移瞬态：直接同步，不平滑
        double cx = this.getX(), cy = this.getY(), cz = this.getZ();
        double dx = cx - this.prevX, dy = cy - this.prevY, dz = cz - this.prevZ;
        boolean transientStep = (dx * dx + dy * dy + dz * dz) > MAX_SPLINE_STEP * MAX_SPLINE_STEP;
        if (transientStep) {
            this.smoothX = target.x;
            this.smoothY = target.y;
            this.smoothZ = target.z;
            return target;
        }
        this.smoothX += (target.x - this.smoothX) * SMOOTH_ALPHA;
        this.smoothY += (target.y - this.smoothY) * SMOOTH_ALPHA;
        this.smoothZ += (target.z - this.smoothZ) * SMOOTH_ALPHA;
        return new Vec3(this.smoothX, this.smoothY, this.smoothZ);
    }

    /** 读取当前平滑位置（车体渲染器用，只读；未初始化时返回 null 表示用原样条位置） */
    public Vec3 getSmoothPositionOrNull() {
        if (!this.smoothInit) return null;
        return new Vec3(this.smoothX, this.smoothY, this.smoothZ);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
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
