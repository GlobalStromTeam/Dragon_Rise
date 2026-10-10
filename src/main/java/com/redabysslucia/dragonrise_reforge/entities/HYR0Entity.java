package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * HYR0：双履带主战坦克（履带曲线见 {@code client/renderer/entity/hyr0/HYR0TrackCurves}）。
 * <p>
 * 本类额外实现"幻影坦克"式的变树功能：
 * <ul>
 *   <li>车辆静止（水平速度为 0）时，按卓越前线的干扰弹键（{@code decoyInputDown}）切换 变树 / 变回车
 *       （该车没有烟幕弹，JSON 里的 {@code HasDecoy} 已删除，所以这个键位空出来给它用）。</li>
 *   <li>冷却时间 = 变树动画长度（{@code animation.turn_to_tree.on} 为 50.125s），按 20 tick/秒 换算。</li>
 *   <li>行进中无法使用；已经变树时一旦开始移动，会强制变回车。</li>
 * </ul>
 * 状态用 {@link #IS_TREE} 同步，客户端在状态翻转时播放对应动画（{@code client.animation.HYR0Animations}）。
 */
@SuppressWarnings("removal")
public class HYR0Entity extends VehicleEntity {

    /** 是否处于"树"形态 */
    public static final EntityDataAccessor<Boolean> IS_TREE =
            SynchedEntityData.defineId(HYR0Entity.class, EntityDataSerializers.BOOLEAN);
    /** 变树/变回车的剩余冷却（tick） */
    public static final EntityDataAccessor<Integer> TREE_COOLDOWN =
            SynchedEntityData.defineId(HYR0Entity.class, EntityDataSerializers.INT);

    /**
     * 冷却时长：变形动作实际耗时 4 秒 → 80 tick。
     * （动画文件里 animation_length 写的是 50.125s，但真正的变形只在前 4 秒内完成，冷却按实际动作时长取。）
     */
    public static final int TREE_COOLDOWN_TICKS = 4 * 20;

    /** 判定"静止"的水平速度阈值（方块/tick），避免浮点残差导致误判 */
    private static final double STILL_SPEED = 0.02;

    /** 服务端：干扰键上一次的状态，用于取上升沿 */
    private boolean lastDecoyInput = false;
    /** 客户端：动画状态跟踪（只在状态翻转时驱动动画） */
    private boolean clientLastTree = false;
    private boolean clientCannonFiring = false;
    private boolean clientMainMgFiring = false;
    private boolean clientPassengerMgFiring = false;
    private boolean clientAnimInitialised = false;

    private final Float[][] PitchAdjustments = {
            {180f, 180f, 180f, 5f, -5f},
            {-180f, -180f, 180f, 5f, -5f},
    };

    public HYR0Entity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_TREE, false);
        this.entityData.define(TREE_COOLDOWN, 0);
    }

    /** 是否处于树形态（客户端动画也读这个） */
    public boolean isTree() {
        return this.entityData.get(IS_TREE);
    }

    public int treeCooldown() {
        return this.entityData.get(TREE_COOLDOWN);
    }

    /** 状态机唯一入口：切换形态并开始冷却 */
    public void setTree(boolean tree) {
        this.entityData.set(IS_TREE, tree);
        this.entityData.set(TREE_COOLDOWN, TREE_COOLDOWN_TICKS);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("IsTree", isTree());
        compound.putInt("TreeCooldown", treeCooldown());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(IS_TREE, compound.getBoolean("IsTree"));
        this.entityData.set(TREE_COOLDOWN, compound.getInt("TreeCooldown"));
    }

    /** 是否处于静止（水平速度≈0） */
    private boolean isStill() {
        Vec3 motion = this.getDeltaMovement();
        return Math.abs(motion.x) < STILL_SPEED && Math.abs(motion.z) < STILL_SPEED;
    }

    /**
     * 是否任意一门武器正在开火。
     * 用卓越前线的 {@code GunData.shootTimer}（开火时被置为 max(+3,5)、每 tick 减 1）判断，
     * 遍历 {@link #getGunDataMap()} 所以主炮/同轴机枪/车顶武器站乃至以后新增的武器都会覆盖。
     */
    private boolean isAnyWeaponFiring() {
        for (var data : this.getGunDataMap().values()) {
            if (data != null && data.shootTimer.get() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void baseTick() {
        super.baseTick();

        int cooldown = treeCooldown();
        if (cooldown > 0) {
            this.entityData.set(TREE_COOLDOWN, cooldown - 1);
        }

        if (!this.level().isClientSide) {
            serverTreeTick();
        } else {
            clientAnimationTick();
        }
    }

    /** 服务端：干扰键（上升沿）切形态 + 开火 / 行进中强制变回车 */
    private void serverTreeTick() {
        // 纯上升沿检测：按住只切换一次，松手再按才切换。
        // （不改 decoyInputDown 本身：客户端可能每 tick 重发输入，置 false 会立刻产生新的上升沿）
        boolean input = this.decoyInputDown();
        boolean pressed = input && !this.lastDecoyInput;
        this.lastDecoyInput = input;

        boolean still = isStill();

        if (isTree()) {
            // 任意一门武器开火 → 暴露，立刻变回车（炮口火焰/后坐会盖不住伪装）
            if (isAnyWeaponFiring()) {
                setTree(false);
                sendMessage("§c开火暴露：幻影伪装已解除");
                return;
            }
            // 开始移动 → 强制变回车
            if (!still) {
                setTree(false);
                return;
            }
        }

        if (!pressed) {
            return;
        }

        if (treeCooldown() > 0) {
            sendMessage("§c变树系统冷却中（" + (treeCooldown() / 20 + 1) + " 秒）");
            return;
        }
        if (!still) {
            sendMessage("§c行进中无法变树（请先停车）");
            return;
        }

        boolean toTree = !isTree();
        setTree(toTree);
        sendMessage(toTree ? "§a幻影伪装：已展开（再次按下干扰键变回）" : "§a幻影伪装：已收起");
    }

    /** 客户端：状态翻转时播放动画；三门武器的开火动画按各自的 shootTimer 驱动 */
    private void clientAnimationTick() {
        boolean tree = isTree();
        if (!clientAnimInitialised) {
            // 首次 tick：把卓越前线默认循环播放的机枪 idle 关掉（改成开火时才播），并同步一次树状态
            clientAnimInitialised = true;
            clientLastTree = tree;
            com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.suppressAutoIdle(this);
            if (tree) {
                com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.playTurnToTree(this, true);
            }
        } else if (tree != clientLastTree) {
            clientLastTree = tree;
            com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.playTurnToTree(this, tree);
        }

        // 主炮：单发动画，上升沿播一次
        boolean cannon = isWeaponFiring("Cannon");
        if (cannon != clientCannonFiring) {
            clientCannonFiring = cannon;
            if (cannon) {
                com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.setFiring(
                        this, com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.CANNON_FIRE, false, true);
            }
        }

        // 同轴机枪：开火期间循环
        boolean mainMg = isWeaponFiring("MainMachineGun");
        if (mainMg != clientMainMgFiring) {
            clientMainMgFiring = mainMg;
            com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.setFiring(
                    this, com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.MAIN_MG_FIRE, true, mainMg);
        }

        // 车顶武器站：开火期间循环
        boolean passengerMg = isWeaponFiring("PassengerMachineGun");
        if (passengerMg != clientPassengerMgFiring) {
            clientPassengerMgFiring = passengerMg;
            com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.setFiring(
                    this, com.redabysslucia.dragonrise_reforge.client.animation.HYR0Animations.PASSENGER_MG_FIRE, true, passengerMg);
        }
    }


    /** 给车长/驾驶员发提示（只在服务端的交互路径调用） */
    private void sendMessage(String text) {
        var driver = this.getFirstPassenger();
        if (driver instanceof net.minecraft.world.entity.player.Player player) {
            player.displayClientMessage(Component.literal(text), true);
        }
    }

    @Override
    public int getTrackAnimationLength() {
        return 80;
    }

    @Override
    public float getTurretMaxHealth() {
        return 100;
    }

    @Override
    public float getWheelMaxHealth() {
        return 100;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }
}
