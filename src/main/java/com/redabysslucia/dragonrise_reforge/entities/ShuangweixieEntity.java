package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.google.gson.JsonObject;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * 双尾蝎无人机（TB-001 一类的大型固定翼无人机），以载具形式接入 superbwarfare。
 * <p>
 * <b>遥控流程</b>（继承 {@link DroneEntity}，与 SBW 原版无人机、本模组 r6_drone 一致）：
 * <ol>
 *   <li>手持监控平板（遥控器）右键无人机 → 绑定（LINKED + CONTROLLER，并把 UUID 写进平板的 LinkedDrone）；</li>
 *   <li>再右键（空中）平板 → Using=true，进入驾驶视角，WASD/空格/鼠标操作。</li>
 * </ol>
 * 数据 JSON 里没有可上机的入口 —— 玩家无法直接骑乘，只能靠遥控器绑定后驾驶。
 * <p>
 * <b>操控</b>：直接调用 SBW 自己的固定翼引擎
 * {@code VehicleEngineUtils.aircraftEngine(vehicle, EngineInfo.Aircraft)}，
 * 参数取载具 JSON 的 {@code EngineInfo}，与 J-20 / F-15 同一个调参入口。
 * 它按"载具上必须有乘员"硬门控（无乘员会清空输入、掉功率、机头下坠），
 * 遥控时由 {@code AircraftEngineRemotePilotMixin} 在仅该方法内部把遥控者当作乘员喂进去。
 * <p>
 * <b>席位</b>：座位数 = 同时遥控人数上限。只有 1 号席位能操控（其余席位与 SBW 其他座位一致：
 * 能看、能用自己席位的武器、能切武器，但不驾驶；由 SeatOneOnly 系列 mixin 在服务端强制）。
 * <p>
 * <b>弹药</b>：完全走 SBW 原生机制 —— 武器从载具容器的弹药物品装填；
 * 操作者背包里的创造弹药盒则通过 {@code InventoryTool.hasCreativeAmmoBoxForVehicle}
 * （载具数据 {@code UsePassengerCreativeAmmoBox} + {@code InventoryToolRemotePilotBoxMixin}）
 * 转成"无限备弹"。
 */
public class ShuangweixieEntity extends DroneEntity {

    /** DroneEntity.baseTick() 末尾的无人机自稳系数（roll *= 0.9），固定翼需要完整坡度，见 {@link #baseTick()} */
    private static final float DRONE_ROLL_DAMP = 0.9f;

    /**
     * 遥控者当前席位号在监控平板 NBT 上的键（每个遥控者各自的平板存一份）。
     * SBW 原生座位切换只服务真乘客，遥控者不在载具上，所以席位由本模组自己维护：
     * 绑定平板时取最小空闲席位，之后 Shift + 数字键切换（见 RemoteSeatSwitchMessage）。
     */
    public static final String DR_REMOTE_SEAT = "DrRemoteSeat";

    public ShuangweixieEntity(EntityType<? extends DroneEntity> type, Level world) {
        super(type, world);
    }

    // ==================== 飞行：交给 SBW 的固定翼引擎 ====================

    @Override
    public void travel() {
        // 注意：两侧都要跑（不能只跑服务端）。
        // SBW 的 roll 是普通字段、不参与 entityData 同步，各客户端是靠"本地也跑一遍引擎"得到坡度的
        // —— 输入位与 DELTA_ROT / MOUSE_SPEED_X/Y 都是同步量，客户端复算结果与服务端一致。
        // 若只在服务端跑，客户端 roll 恒为 0，遥控视角的 cameraRoll 就永远为 0
        // （表现为：视角跟着抬头低头，但不跟着压坡度滚转）。
        EngineInfo cached = this.getEngineInfo();
        EngineInfo.Aircraft aircraft = cached instanceof EngineInfo.Aircraft ac
                ? ac
                : this.dr$aircraftConfig();
        this.setEngineInfo(aircraft);

        // SBW 的固定翼引擎（与 J-20 同一套代码）。"是否有乘员"的门控由 mixin 放行。
        VehicleEngineUtils.aircraftEngine(this, aircraft);
    }

    @Override
    public void baseTick() {
        super.baseTick();   // 内含 VehicleEntity.baseTick → travel()（上面那套飞机引擎）

        // DroneEntity.baseTick() 在 super 之后会执行 setZRot(getRoll() * 0.9f)（无人机自稳阻尼）。
        // 那是四轴的自稳，对固定翼来说每 tick 削掉 10% 坡度会压不出杆，这里反算回去。
        this.setZRot(this.getRoll() / DRONE_ROLL_DAMP);

        // 地面自动摆正。
        // SBW 的飞机引擎里舵面输入和"自动回正"都写在 `if (!onGround())` 里，
        // 也就是停在地面上引擎完全不碰姿态；加上上面那行抵消了四轴自稳阻尼，
        // 于是会出现"带坡度落地后一直斜着不起来"。这里补一个只在接地时生效的缓慢回正
        // （不影响地面滑跑：地面上引擎本来也不响应鼠标姿态输入）。
        if (this.onGround()) {
            this.setXRot(this.getXRot() * 0.75f);
            this.setZRot(this.getRoll() * 0.75f);
        }
    }

    /**
     * 用载具 JSON 的 {@code EngineInfo} 构造 SBW 的飞机引擎配置。
     * 不走 SBW 的序列化（那是 Kotlin 内部路径），直接逐项取值，缺省值取本模组飞机的常用值。
     */
    private EngineInfo.Aircraft dr$aircraftConfig() {
        JsonObject cfg = this.computed().getEngineInfo();

        EngineInfo.Aircraft ac = new EngineInfo.Aircraft();
        ac.setIncrement(dr$float(cfg, "Increment", 1.2f));
        ac.setDecrement(dr$float(cfg, "Decrement", 1.0f));
        ac.setPitchSpeed(dr$float(cfg, "PitchSpeed", 1.0f));
        ac.setYawSpeed(dr$float(cfg, "YawSpeed", 1.0f));
        ac.setRollSpeed(dr$float(cfg, "RollSpeed", 1.0f));
        // 升力系数：无人机翼载低，适当放大以便短场起飞；太飘就调小
        ac.setLiftSpeed(dr$float(cfg, "LiftSpeed", 1.25f));
        ac.setSpeed(dr$float(cfg, "Speed", 1.0f));
        ac.setClampPitch(dr$float(cfg, "ClampPitch", 60f));
        ac.setClampRoll(dr$float(cfg, "ClampRoll", 60f));
        ac.setSpeedRate(dr$float(cfg, "SpeedRate", 1.0f));
        ac.setGearRotateAngle(dr$float(cfg, "GearRotateAngle", 85f));
        ac.setHasGear(dr$bool(cfg, "HasGear", false));
        ac.setHasStukaSound(dr$bool(cfg, "HasStukaSound", false));
        ac.setResistance(dr$float(cfg, "Resistance", 1.0f));
        ac.setEnergyCostRate(dr$double(cfg, "EnergyCostRate", 256.0));
        ac.setEngineSoundVolume(dr$float(cfg, "EngineSoundVolume", 0.9f));
        ac.setEngineStartSound(SoundEvents.EMPTY);
        return ac;
    }

    private static float dr$float(JsonObject cfg, String key, float fallback) {
        if (cfg == null || !cfg.has(key)) {
            return fallback;
        }
        try {
            return cfg.get(key).getAsFloat();
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean dr$bool(JsonObject cfg, String key, boolean fallback) {
        if (cfg == null || !cfg.has(key)) {
            return fallback;
        }
        try {
            return cfg.get(key).getAsBoolean();
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double dr$double(JsonObject cfg, String key, double fallback) {
        if (cfg == null || !cfg.has(key)) {
            return fallback;
        }
        try {
            return cfg.get(key).getAsDouble();
        } catch (Exception e) {
            return fallback;
        }
    }

    // ==================== 席位 ====================

    /**
     * 遥控者坐在几号位。SBW 的座位相关逻辑（HUD、武器站、相机）全部走 {@code getSeatIndex}，
     * 遥控者不是真乘客 → 原实现返回 -1，HUD 会认为"没有座位"、座位相机也拿不到值。
     * 这里让遥控者按自己当前席位（存在平板 NBT 上）进入 SBW 的座位体系。
     */
    @Override
    public int getSeatIndex(net.minecraft.world.entity.Entity entity) {
        int real = super.getSeatIndex(entity);
        if (real >= 0) {
            return real;        // 真乘客优先（本机正常情况下不会有）
        }
        if (entity instanceof Player player) {
            return this.dr$remoteSeat(player);      // 非遥控者返回 -1
        }
        return real;
    }

    /**
     * 座位/乘员列表（HUD 左下角那串 [1] [2] …）显示遥控操作者。
     * <p>
     * 官方 {@code VehicleHudOverlay.renderPassengerInfo} 读的就是这个方法，
     * 而遥控者不是真乘客，原实现里对应席位恒为 null，列表只显示 "---"。
     * 这里返回一份副本并把遥控者填进各自席位（真乘客优先，不覆盖）。
     * <p>
     * 注意：座位分配/上下机等逻辑用的是内部字段而非本方法，因此不受影响。
     * 客户端只能看到本地玩家的物品栏，所以这份补全在客户端只对本机操作者生效
     * （多人时其他操作者需服务端同步才能显示）。
     */
    @Override
    public java.util.List<net.minecraft.world.entity.Entity> getOrderedPassengers() {
        java.util.List<net.minecraft.world.entity.Entity> out =
                new java.util.ArrayList<>(super.getOrderedPassengers());
        if (this.level() == null) {
            return out;
        }
        for (Player other : this.level().players()) {
            int seat = this.dr$remoteSeat(other);
            if (seat >= 0 && seat < out.size() && out.get(seat) == null) {
                out.set(seat, other);
            }
        }
        return out;
    }

    /** 本机座位数 = 同时可遥控的人数上限 */
    private int dr$seatCount() {
        return Math.max(1, this.getMaxPassengers());
    }

    /** entity 是否正在遥控本机（监控平板处于 Using + Linked 且指向本机） */
    private boolean dr$isRemoteController(Player player) {
        return this.dr$remoteSeat(player) >= 0;
    }

    /**
     * 该玩家当前占用的席位号；没在遥控本机时返回 -1。
     * 席位存在玩家主手监控平板的 NBT 上（每个遥控者各自一份），键 {@link #DR_REMOTE_SEAT}。
     */
    public int dr$remoteSeat(Player player) {
        if (player == null) {
            return -1;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return -1;
        }
        // 注意：本方法是每帧都会被调用的热路径（HUD / 座位相机），
        // 只能用 getTag() 读，不能用 getOrCreateTag() 给物品凭空造 NBT。
        var tag = stack.getTag();
        if (tag == null) {
            return -1;
        }
        if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)
                || !this.getStringUUID().equals(tag.getString(MonitorItem.LINKED_DRONE))) {
            return -1;
        }
        int seat = tag.contains(DR_REMOTE_SEAT) ? tag.getInt(DR_REMOTE_SEAT) : 0;
        return net.minecraft.util.Mth.clamp(seat, 0, this.dr$seatCount() - 1);
    }

    /** 某席位是否已被别的遥控者占用 */
    private boolean dr$seatTakenByOther(Player player, int seat) {
        for (Player other : this.level().players()) {
            if (other != player && this.dr$remoteSeat(other) == seat) {
                return true;
            }
        }
        return false;
    }

    /** 绑定时分配席位：取最小空闲位；已经在遥控本机的保留原席位 */
    private void dr$assignSeatOnBind(Player player, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        boolean alreadyOurs = this.getStringUUID().equals(tag.getString(MonitorItem.LINKED_DRONE));
        if (alreadyOurs && tag.contains(DR_REMOTE_SEAT)) {
            return;
        }
        for (int i = 0; i < this.dr$seatCount(); i++) {
            if (!this.dr$seatTakenByOther(player, i)) {
                tag.putInt(DR_REMOTE_SEAT, i);
                return;
            }
        }
        tag.putInt(DR_REMOTE_SEAT, 0);
    }

    /**
     * 切换遥控席位（服务端）：目标席位必须在本机座位数范围内、且未被别的遥控者占用。
     *
     * @return 切换后的权威席位号（无论成功与否，客户端以它为准）
     */
    public int dr$switchRemoteSeat(Player player, int index) {
        int current = this.dr$remoteSeat(player);
        if (current < 0) {
            return current;                     // 不在遥控本机，忽略
        }
        if (index < 0 || index >= this.dr$seatCount() || index == current) {
            return current;
        }
        if (this.dr$seatTakenByOther(player, index)) {
            player.displayClientMessage(
                    Component.translatable("message.dragonrise_reforge.seat_taken", index + 1), true);
            return current;
        }

        player.getMainHandItem().getOrCreateTag().putInt(DR_REMOTE_SEAT, index);
        player.displayClientMessage(
                Component.translatable("message.dragonrise_reforge.seat_switched", index + 1), true);
        return index;
    }

    /**
     * 还能不能再接纳一个遥控者：
     * 已经在遥控本机的人放行（用于解绑/重绑）；否则看当前有效遥控者数量是否已达座位数上限。
     */
    private boolean dr$canAcceptController(Player player, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (this.getStringUUID().equals(tag.getString(MonitorItem.LINKED_DRONE))) {
            return true;
        }
        int active = 0;
        for (Player other : this.level().players()) {
            if (other != player && this.dr$isRemoteController(other)) {
                active++;
            }
        }
        return active < this.dr$seatCount();
    }

    // ==================== 交互 ====================

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 监控平板（遥控器）：绑定/解除绑定走 SBW 原逻辑；
        // 但先查容量 —— 座位数就是同时遥控人数上限（getMaxPassengers）。
        if (stack.is(ModItems.MONITOR.get())) {
            if (!this.level().isClientSide()) {
                if (!this.dr$canAcceptController(player, stack)) {
                    player.displayClientMessage(
                            Component.translatable("message.dragonrise_reforge.remote_slots_full",
                                    this.dr$seatCount()), true);
                    return InteractionResult.FAIL;
                }
                // 分配席位（取最小空闲位；已在遥控本机的保留原席位），并把权威席位同步给客户端
                this.dr$assignSeatOnBind(player, stack);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    int seat = stack.getOrCreateTag().contains(DR_REMOTE_SEAT)
                            ? stack.getOrCreateTag().getInt(DR_REMOTE_SEAT) : 0;
                    com.redabysslucia.dragonrise_reforge.network.ModNetwork.PACKET_HANDLER.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                            new com.redabysslucia.dragonrise_reforge.network.message.RemoteSeatSyncMessage(seat));
                }
            }
            return super.interact(player, hand);
        }

        // 潜行 + 撬棍 = 打包回收（返还本模组双尾蝎的集装箱物品）。
        // 必须用撬棍：空手潜行右键要留给"打开弹药容器"。
        if (player.isShiftKeyDown() && stack.is(ModTags.Items.TOOLS_CROWBAR)) {
            if (!this.level().isClientSide()) {
                Dragonrise_reforge.LOGGER.info("[DR-CONTAINER] 撬棍回收 {}", this.getStringUUID());
                this.dr$recycle(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 潜行 + 空手 = 打开载具弹药容器（装填弹药）
        // 必须显式调 openMenu：父类 DroneEntity.interact 会把"潜行 + 空手/撬棍"直接当成
        // 拆除 SBW 原版无人机，那条分支在 VehicleEntity 的 openMenu 分支之前，
        // 所以走 super.interact 永远打不开容器。
        if (player.isShiftKeyDown() && stack.isEmpty() && this.hasMenu() && this.getContainerSize() > 0) {
            if (!this.level().isClientSide()) {
                Dragonrise_reforge.LOGGER.info("[DR-CONTAINER] 打开弹药容器 {} (size={}, type={})",
                        this.getStringUUID(), this.getContainerSize(), this.computed().getVehicleContainerType());
                this.openMenu(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // 其余一律不响应：本机是无人机，只能遥控驾驶，不允许右键上机
        return InteractionResult.PASS;
    }

    /** 拆除回收：解除监控平板链接 → 返还集装箱物品 → 移除实体（仅服务端调用） */
    private void dr$recycle(Player player) {
        player.getInventory().items.stream()
                .filter(s -> s.is(ModItems.MONITOR.get()))
                .forEach(s -> {
                    if (s.getOrCreateTag().getString(MonitorItem.LINKED_DRONE).equals(this.getStringUUID())) {
                        MonitorItem.disLink(s, player);
                    }
                });

        ItemHandlerHelper.giveItemToPlayer(player,
                ContainerBlockItem.createInstance(ModEntities.SHUANGWEIXIE.get()));
        this.discard();
    }

    /** 创造模式中键取方块 / 拆解预览：给出本载具自己的集装箱物品 */
    @Override
    public ItemStack getPickResult() {
        return ContainerBlockItem.createInstance(ModEntities.SHUANGWEIXIE.get());
    }
}
