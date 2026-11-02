package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.google.gson.JsonObject;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 本模组"遥控无人机"的公共底座（双尾蝎、DGSW MK L2 等全部继承它）。
 * <p>
 * <b>为什么要有这个类：</b>遥控（监控平板）这条链路 —— 绑定/解绑、席位分配、遥控开火、
 * 遥控 HUD、遥控相机、打包回收 —— 与具体机型无关，只与"玩家不在机上"这一件事有关。
 * 之前这些代码全写在 {@link ShuangweixieEntity} 里，第二架遥控机出现时就必须整份复制，
 * 所以这里把它原样抽出来，机型类只保留自己的飞行/武器特性。
 * <p>
 * <b>遥控流程</b>（与 SBW 原版无人机一致，不需要任何新物品）：
 * <ol>
 *   <li>手持监控平板右键无人机 → 绑定（LINKED + CONTROLLER，UUID 写进平板 LinkedDrone）；</li>
 *   <li>再右键（空中）平板 → Using=true，进入驾驶视角；</li>
 *   <li>Shift + 数字键切遥控席位，数字键/滚轮切武器，左键开火。</li>
 * </ol>
 * 席位号存在<b>每个遥控者自己的</b>平板 NBT 上（键 {@link #DR_REMOTE_SEAT}），
 * 因为遥控者不是真乘客，SBW 原生的 {@code changeSeat} 走不到。
 * <p>
 * <b>1.21.1 注意</b>：{@code ItemStack#getTag()/getOrCreateTag()} 已被移除，
 * 读写平板 NBT 一律走 {@link NBTTool#getTag}（读的是副本）+ {@link NBTTool#saveTag}（写回），
 * 只改 tag 不 saveTag 会被静默丢弃。
 * <p>
 * <b>子类需要提供的只有一件东西：</b>打包回收时返还哪个集装箱物品（{@link #dr$containerType()}）。
 */
public abstract class AbstractRemotePilotDrone extends DroneEntity {

    /**
     * 遥控者当前席位号在监控平板 NBT 上的键（每个遥控者各自的平板存一份）。
     * SBW 原生座位切换只服务真乘客，遥控者不在载具上，所以席位由本模组自己维护：
     * 绑定平板时取最小空闲席位，之后 Shift + 数字键切换（见 RemoteSeatSwitchMessage）。
     */
    public static final String DR_REMOTE_SEAT = "DrRemoteSeat";

    protected AbstractRemotePilotDrone(EntityType<? extends DroneEntity> type, Level world) {
        super(type, world);
    }

    // ==================== 子类必须提供 ====================

    /** 打包回收 / 中键取物时返还的实体类型（各机型自己的集装箱物品） */
    protected abstract EntityType<?> dr$containerType();

    // ==================== 受击反馈（音效） ====================

    /**
     * 让本机被攻击时发出受击音效。
     * <p>
     * SBW 的无人机基类把受击音关掉了（{@code DroneEntity.shouldSendHitSounds() → false}），
     * 本机继承它之后白拿了那个 false。本方法<b>只</b>控制"玩家攻击键打到机身"这一声金属音，
     * 不影响引擎音、开火音。受击<b>粒子</b>由载具数据 {@code SendHitParticles} 控制。
     */
    @Override
    public boolean shouldSendHitSounds() {
        return true;
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
        // 只能读 tag，不能写回去（saveTag 会触发物品同步）。
        var tag = NBTTool.getTag(stack);
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
        // 1.21.1：NBTTool.getTag 返回的是副本，改完必须 saveTag 写回，否则修改被静默丢弃。
        var tag = NBTTool.getTag(stack);
        boolean alreadyOurs = this.getStringUUID().equals(tag.getString(MonitorItem.LINKED_DRONE));
        if (alreadyOurs && tag.contains(DR_REMOTE_SEAT)) {
            return;
        }
        int assigned = 0;
        for (int i = 0; i < this.dr$seatCount(); i++) {
            if (!this.dr$seatTakenByOther(player, i)) {
                assigned = i;
                break;
            }
        }
        if (assigned != tag.getInt(DR_REMOTE_SEAT)) {
            tag.putInt(DR_REMOTE_SEAT, assigned);
            NBTTool.saveTag(stack, tag);
        }
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

        // 1.21.1：读副本 -> 改 -> saveTag 写回
        ItemStack held = player.getMainHandItem();
        var seatTag = NBTTool.getTag(held);
        seatTag.putInt(DR_REMOTE_SEAT, index);
        NBTTool.saveTag(held, seatTag);
        player.displayClientMessage(
                Component.translatable("message.dragonrise_reforge.seat_switched", index + 1), true);
        return index;
    }

    /**
     * 还能不能再接纳一个遥控者：
     * 已经在遥控本机的人放行（用于解绑/重绑）；否则看当前有效遥控者数量是否已达座位数上限。
     */
    private boolean dr$canAcceptController(Player player, ItemStack stack) {
        var tag = NBTTool.getTag(stack);
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
                    var seatTag = NBTTool.getTag(stack);
                    int seat = seatTag.contains(DR_REMOTE_SEAT) ? seatTag.getInt(DR_REMOTE_SEAT) : 0;
                    PacketDistributor.sendToPlayer(sp,
                            new com.redabysslucia.dragonrise_reforge.network.message.RemoteSeatSyncMessage(seat));
                }
            }
            return super.interact(player, hand);
        }

        // 潜行 + 撬棍 = 打包回收（返还本机的集装箱物品）。
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
        // 拆除 SBW 原版无人机，那条分支在 VehicleEntity 的 openMenu 分支之前。
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
                    var linkTag = NBTTool.getTag(s);
                    if (linkTag.getString(MonitorItem.LINKED_DRONE).equals(this.getStringUUID())) {
                        // SBW 0.8.9.2 的 disLink 只改传入的 tag、不回写 ItemStack，必须自己 saveTag
                        MonitorItem.disLink(linkTag, player);
                        NBTTool.saveTag(s, linkTag);
                    }
                });

        ItemHandlerHelper.giveItemToPlayer(player, ContainerBlockItem.createInstance(this.dr$containerType()));
        this.discard();
    }

    /** 创造模式中键取方块 / 拆解预览：给出本载具自己的集装箱物品 */
    @Override
    public ItemStack getPickResult() {
        return ContainerBlockItem.createInstance(this.dr$containerType());
    }

    // ==================== 遥控相机朝向 ====================

    /**
     * 遥控第一人称相机的偏航/俯仰。
     * <p>
     * {@code RemoteAircraftCameraMixin} 会接管 {@code Camera.setup}（HEAD）把相机摆到座位机位，
     * 朝向默认用机体姿态。机型可以覆写这两个方法改造视线，例如 {@link DgswMkL2Entity}
     * 把俯仰加上炮管俯角 —— 鼠标推的是炮管，视线得跟着炮管，否则看不到自己在打哪。
     */
    public float dr$cameraYaw(float partialTicks) {
        return this.getYaw(partialTicks);
    }

    /** @see #dr$cameraYaw(float) */
    public float dr$cameraPitch(float partialTicks) {
        return this.getPitch(partialTicks);
    }

    // ==================== 载具数据取值（机型调参统一入口） ====================

    /**
     * 机型自用参数所在的 JSON 块。
     * <p>
     * 卓越前线的 {@code DefaultVehicleData} 把 {@code EngineInfo} 存成<b>自由格式 JsonObject</b>，
     * 各机型按需往里塞键 —— 双尾蝎那套飞机参数（{@code Increment/LiftSpeed/ClampPitch…}）就是这么取的。
     * 本机的 {@code MouseToBarrel / LockBodyPitch / CameraFollowBarrel / ThirdPersonHud} 也在这里。
     */
    protected com.google.gson.JsonObject dr$cfg() {
        return this.computed().getEngineInfo();
    }

    /**
     * HUD 是否使用<b>第三人称那套样式</b>（数据 {@code EngineInfo.ThirdPersonHud}，缺省 false）。
     * <p>
     * true → 进入遥控时把相机类型设成 {@code THIRD_PERSON_BACK}：SBW 的 {@code AircraftHud}
     * 与 {@code VehicleCrosshairOverlay} 都会走第三人称分支（小圆环 + 命中点准星 + 武器信息），
     * 不再画第一人称那套座舱式 HUD（大 HUD 底板 / 罗盘 / 俯仰梯）。
     * 机位与朝向不受影响 —— 遥控相机由 {@code RemoteAircraftCameraMixin} 在 {@code Camera.setup}
     * 的 HEAD 接管，它把 {@code FIRST_PERSON} 与 {@code THIRD_PERSON_BACK} 当作同一档处理。
     */
    public boolean dr$thirdPersonHud() {
        return dr$bool(this.dr$cfg(), "ThirdPersonHud", false);
    }

    /**
     * 从载具 JSON 的某个 JsonObject（通常是 {@code computed().getEngineInfo()}）里读数值。
     * <p>
     * 卓越前线的 {@code DefaultVehicleData} 把 {@code EngineInfo} 存成<b>自由格式 JsonObject</b>，
     * 各机型按自己的需要往里塞键，读的时候自己带缺省值 —— 双尾蝎那套飞机参数就是这么取的。
     *
     * @param cfg      {@code computed().getEngineInfo()}，可为 null
     * @param key      键名
     * @param fallback 键不存在或类型不对时返回的值
     */
    protected static float dr$float(JsonObject cfg, String key, float fallback) {
        if (cfg == null || !cfg.has(key)) {
            return fallback;
        }
        try {
            return cfg.get(key).getAsFloat();
        } catch (Exception e) {
            return fallback;
        }
    }

    /** @see #dr$float(JsonObject, String, float) */
    protected static boolean dr$bool(JsonObject cfg, String key, boolean fallback) {
        if (cfg == null || !cfg.has(key)) {
            return fallback;
        }
        try {
            return cfg.get(key).getAsBoolean();
        } catch (Exception e) {
            return fallback;
        }
    }

    /** @see #dr$float(JsonObject, String, float) */
    protected static double dr$double(JsonObject cfg, String key, double fallback) {
        if (cfg == null || !cfg.has(key)) {
            return fallback;
        }
        try {
            return cfg.get(key).getAsDouble();
        } catch (Exception e) {
            return fallback;
        }
    }
}
