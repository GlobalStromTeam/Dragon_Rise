package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.google.gson.JsonObject;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * DGSW MK L2 四轴察打无人机。
 * <p>
 * <b>模型</b>：DGSW_MK_L2（{@code 训练模型/DGSW_MK_L2.json}），机腹挂一门 30mm 机炮，
 * 模型里只有名字为 {@code barrel} 的骨骼、<b>没有</b> {@code turret} 骨骼。
 * <p>
 * <b>操控 = 卓越前线（superbwarfare）的无人机</b>：继承 {@link DroneEntity}，飞行逻辑全部复用
 * {@link DroneEntity#travel()}（鼠标左右转向、空格/潜行升降、WASD 倾斜推进、碰撞与耗电），
 * 遥控方式（监控平板绑定 → 右键平板 Using → 进视角）也走官方链路。
 * <p>
 * <b>与官方无人机的唯一区别：俯仰轴的归属</b>。官方把鼠标上下算在机体上
 * （{@code xRot += 0.5 * mouseMoveSpeedY}），本机默认把这步挪到 {@code barrel} 上：
 * 机体保持水平、鼠标上下推炮管、视线跟着炮管（可在数据里关掉，见下）。
 * <p>
 * <b>武器 = 只有 barrel，没有 turret</b>：数据里不填 {@code TurretPos}，
 * 于是 {@code hasTurret() = false}，SBW 的炮塔随动/偏航机构都不介入，
 * {@code getTurretTransform()} 退回机体变换；只填 {@code BarrelPos}（= 模型 barrel 骨骼枢轴），
 * 炮弹、barrel 骨骼、镜头都绕它转，{@code ShootPos.Transform = "Barrel"}
 * 让弹道与炮口严格一致。主武器数据照抄卓越前线 BMP-2 的 2A42 30mm 机炮。
 *
 * <h2>载具 JSON 可调项（{@code data/dragonrise_reforge/sbw/vehicles/dgsw_mk_l2.json}）</h2>
 * <b>原生字段</b>（卓越前线自己解析，改完重载数据包即可，不用重新编译）：
 * <ul>
 *   <li>{@code TurretPitchRange} —— 炮管俯仰行程。本机 {@code [-60, 0]} = 俯角 60°、仰角 0°；</li>
 *   <li>{@code TurretTurnSpeed[0]} —— 炮管每 tick 最大俯仰角速度（本机 6°/tick）；</li>
 *   <li>{@code BarrelPos} —— 炮管旋转中心（= 模型 barrel 骨骼枢轴，换模型要跟着改）；</li>
 *   <li>{@code Weapons.Cannon.ShootPos} —— 出膛点/弹道方向（本机 {@code Transform: "Barrel"}）；</li>
 *   <li>{@code Seats[0].CameraPos} —— 机位，本机挂在 {@code Transform: "Barrel"} 上，跟着炮管转。</li>
 * </ul>
 * <b>本机型私有字段</b>（放在 {@code EngineInfo} 里，和双尾蝎那套飞机参数同一个入口，
 * 由 {@link #dr$cfg()} 读取）：{@code MouseToBarrel} / {@code LockBodyPitch} / {@code CameraFollowBarrel}，
 * 缺省值写在下面三个 {@code DEFAULT_*} 常量里；键不存在就用缺省值，不会报错。
 */
public class DgswMkL2Entity extends AbstractRemotePilotDrone {

    // ==================== 数据缺省值（JSON 里没写时用这些） ====================

    /**
     * {@code EngineInfo.MouseToBarrel} —— 鼠标上下 → 炮管俯仰的响应系数
     * （每 tick 角度 = 系数 × {@code mouseMoveSpeedY}）。
     * <p>
     * 缺省 0.5，与官方无人机 {@code DroneEntity.travel()} 里推机体用的系数完全一致
     * （{@code xRot += 0.5f * mouseMoveSpeedY}）—— 只是那一步现在推的是炮管，
     * 手感与原版无人机的抬头低头相同。每 tick 增量还会被 {@code TurretTurnSpeed[0]} 夹一次。
     */
    private static final float DEFAULT_MOUSE_TO_BARREL = 0.5f;

    /**
     * {@code EngineInfo.LockBodyPitch} —— 机体是否锁平（缺省 true）。
     * <ul>
     *   <li>{@code true}：鼠标上下只推炮管，机体悬停姿态保持水平（本机默认设计）；</li>
     *   <li>{@code false}：回到官方无人机行为 —— 机体跟着鼠标抬头低头，炮管不再吃鼠标输入。</li>
     * </ul>
     */
    private static final boolean DEFAULT_LOCK_BODY_PITCH = true;

    /**
     * {@code EngineInfo.CameraFollowBarrel} —— 遥控第一人称视线是否跟着炮管俯仰（缺省 true）。
     * <p>
     * {@code true}：屏幕中央就是炮口方向，HUD 准星（跟着 barrel 弹道画）落在准心附近；
     * {@code false}：视线只按机体姿态（水平），炮管自己低头 —— 想要"固定平视 + 活动炮管"就设成 false。
     */
    private static final boolean DEFAULT_CAMERA_FOLLOW_BARREL = true;

    /**
     * 上一 tick 的炮管俯角（仅用于渲染插值）。
     * <p>
     * SBW 的 {@code turretXRotO} 是在 {@code VehicleEntity.baseTick} 的<b>末尾</b>才刷新的
     * （{@code turretXRotO = turretXRot}），渲染时 {@code lerp(partialTick, O, cur)} 恒等于 cur，
     * 相机就会跟着 20Hz 的 tick 一跳一跳。这里自己记一份"tick 开始时的值"，
     * 相机俯仰用它插值，鼠标推炮管时视线是连续的。
     */
    private float dr$turretXRotPrev;

    public DgswMkL2Entity(EntityType<? extends DroneEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected EntityType<?> dr$containerType() {
        return ModEntities.DGSW_MK_L2.get();
    }

    // ==================== 数据读取 ====================

    // 数据块本身在基类 {@link AbstractRemotePilotDrone#dr$cfg()}（= computed().getEngineInfo()），
    // 数据包重载（{@code /reload} 或重新进世界）后立即生效，不用重启游戏。

    /** {@code EngineInfo.MouseToBarrel}，缺省 {@link #DEFAULT_MOUSE_TO_BARREL} */
    private float dr$mouseToBarrel() {
        return dr$float(this.dr$cfg(), "MouseToBarrel", DEFAULT_MOUSE_TO_BARREL);
    }

    /** {@code EngineInfo.LockBodyPitch}，缺省 {@link #DEFAULT_LOCK_BODY_PITCH} */
    private boolean dr$lockBodyPitch() {
        return dr$bool(this.dr$cfg(), "LockBodyPitch", DEFAULT_LOCK_BODY_PITCH);
    }

    /** {@code EngineInfo.CameraFollowBarrel}，缺省 {@link #DEFAULT_CAMERA_FOLLOW_BARREL} */
    private boolean dr$cameraFollowBarrel() {
        return dr$bool(this.dr$cfg(), "CameraFollowBarrel", DEFAULT_CAMERA_FOLLOW_BARREL);
    }

    // ==================== 俯仰轴：机体让给炮管 ====================

    /**
     * 官方四轴逻辑照跑；{@code LockBodyPitch = true}（默认）时把"鼠标上下 → 机体俯仰"
     * 换成"鼠标上下 → 炮管俯仰"。
     * <p>
     * {@link DroneEntity#travel()} 里改机体俯仰的只有一句
     * {@code this.xRot = (this.xRot + 0.5f * mouseMoveSpeedY).coerceIn(-10f, 90f)}，
     * 所以先记下进入本方法时的机体俯仰，等 {@code super.travel()} 跑完再写回去 ——
     * 机体保持水平，其余（左右转向、升降、碰撞、水面扣血）全是官方原样。
     * <p>
     * 注意：两侧都要跑（客户端靠本地复算得到同样的 {@code turretXRot}，
     * 与服务端经 additionalData 同步下来的值一致，开火方向不会漂）。
     */
    @Override
    public void travel() {
        if (!this.dr$lockBodyPitch()) {
            // 数据要求"跟官方无人机一样"：机体照旧跟鼠标抬头低头，炮管不吃鼠标输入
            super.travel();
            return;
        }

        float bodyPitch = this.getXRot();
        super.travel();     // 官方无人机：鼠标转向 / 升降 / 倾斜推进 / 碰撞
        this.setXRot(bodyPitch);        // 机体不跟着鼠标抬头低头
        this.dr$aimBarrelByMouse();     // 鼠标上下改去推炮管
    }

    /**
     * 鼠标上下 → 炮管俯仰。
     * <p>
     * {@code mouseMoveSpeedY} 是 SBW 的同步量（{@code MOUSE_SPEED_Y}，每 tick ×0.95 衰减），
     * <b>正值 = 鼠标下移</b>；{@code turretXRot} 同样是"正 = 低头"，所以直接相加，符号不用翻。
     * <p>
     * 三个数全部来自数据，Java 里不写死：
     * <ul>
     *   <li>系数 {@code EngineInfo.MouseToBarrel}（缺省 0.5）；</li>
     *   <li>每 tick 增量上限 {@code TurretTurnSpeed[0]}（本机 6°/tick），防止甩鼠标把炮管甩飞；</li>
     *   <li>可用区间 {@code [-TurretPitchRange[1], -TurretPitchRange[0]]}
     *       （{@code [-60, 0]} → {@code [0, 60]}）：上界 0 就是仰角 0，鼠标往上推炮管停在水平线。</li>
     * </ul>
     */
    private void dr$aimBarrelByMouse() {
        float mouseY = this.getMouseMoveSpeedY();
        if (mouseY == 0f) {
            return;
        }
        float upLimit = -this.getTurretMaxPitch();      // 仰角上限：数据 MaxPitch = 0  → 0
        float downLimit = -this.getTurretMinPitch();    // 俯角上限：数据 MinPitch = -60 → 60
        float turnSpeed = this.getTurretTurnXSpeed();   // 数据 TurretTurnSpeed[0]    = 6°/tick

        float delta = Mth.clamp(this.dr$mouseToBarrel() * mouseY, -turnSpeed, turnSpeed);
        this.setTurretXRot(Mth.clamp(this.getTurretXRot() + delta, upLimit, downLimit));
    }

    /**
     * 兜底限位：把 {@code turretXRot} 夹进数据区间（防止外部写入越界，
     * 例如控制台的 {@code /data} 指令或以后改动数据）。不改变瞄准方向，只夹范围。
     */
    @Override
    public void baseTick() {
        this.dr$turretXRotPrev = this.getTurretXRot();
        super.baseTick();
        if (!this.level().isClientSide()) {
            float upLimit = -this.getTurretMaxPitch();
            float downLimit = -this.getTurretMinPitch();
            float clamped = Mth.clamp(this.getTurretXRot(), upLimit, downLimit);
            if (clamped != this.getTurretXRot()) {
                this.setTurretXRot(clamped);
            }
        }
    }

    // ==================== 遥控相机 ====================

    /**
     * 遥控第一人称相机：机位取座位数据的 {@code CameraPos}
     * （本机挂在 {@code Transform: "Barrel"} 上，跟着炮管一起绕 BarrelPos 转，不会穿模），
     * 而不是 {@link DroneEntity} 默认的"机体后方 2 格"追尾位 ——
     * 本机机身 5 格见方，追尾位会贴在尾撑上，且机腹机炮完全看不到。
     * <p>
     * 第三人称 / 缩放仍然走父类（官方追尾视角），与双尾蝎的处理保持一致。
     */
    @Override
    public Vec3 getCameraPosition(float partialTicks, Player player, boolean zoom, boolean isFirstPerson) {
        if (isFirstPerson && player != null) {
            Vec3 seatCam = this.getCameraPos(player, partialTicks);
            if (seatCam != null) {
                return seatCam;
            }
        }
        return super.getCameraPosition(partialTicks, player, zoom, isFirstPerson);
    }

    /**
     * 载具俯仰 = <b>机体俯仰 + 炮管俯角</b>（两者都是"正 = 低头"，直接相加）。
     * 是否加炮管这一段由 {@code EngineInfo.CameraFollowBarrel} 控制。
     * <p>
     * <b>为什么改在这里：</b>卓越前线的遥控相机对无人机是<b>直接读 {@code drone.getPitch()}</b> 的，
     * 不经过 {@code getCameraRotation}。所以想让视线跟着炮管，最稳的做法就是让 {@code getPitch()}
     * 本身带上炮管俯角 —— 无论谁先取消相机注入，读到的都是同一个值。
     * <p>
     * 副作用只有一处需要处理：{@code GeoVehicleRenderer} 也用 {@code getPitch()} 转机体骨架，
     * 所以 {@code DgswMkL2Renderer.tickVariables} 里会把骨架的俯仰改回 {@link #dr$bodyPitch(float)}。
     * 其余在用 {@code getPitch()} 的地方（HUD 人工地平线/俯仰梯、自由视角）本来就该按"视线的俯仰"画。
     */
    @Override
    public float getPitch(float partialTicks) {
        float barrel = this.dr$cameraFollowBarrel()
                ? Mth.lerp(partialTicks, this.dr$turretXRotPrev, this.getTurretXRot())
                : 0f;
        return this.dr$bodyPitch(partialTicks) + barrel;
    }

    /**
     * 机体自身的俯仰（<b>不含</b>炮管俯角）。
     * <p>
     * 默认配置下机体不跟鼠标动，正常恒为 0；渲染机体骨架时必须用这个值，
     * 否则整个机身会跟着炮管一起栽下去。
     */
    public float dr$bodyPitch(float partialTicks) {
        return super.getPitch(partialTicks);
    }

    // ==================== 出膛点诊断 ====================

    /** 上一次开火日志的 tick（限流：连发时别刷屏） */
    private int dr$lastShotLogTick = -100;

    /**
     * 开火时把"出膛点 / 相机 / 两者差"打出来（仅服务端，每 5 tick 最多一条）。
     * <p>
     * 覆盖的是 {@code VehicleFireMessage} 走的那个重载（{@code weaponName == null}）。
     * 想知道弹到底从哪冒出来、和相机差多少，看日志里这一行就够了：
     * <pre>
     * [DR-SHOT] 出膛点=(x,y,z) 相机=(x,y,z) 出膛-相机=(dx,dy,dz) 距离=d 方向=(x,y,z) 俯角=r
     * </pre>
     * 进游戏试射，对着 {@code Weapons.Cannon.ShootPos.Positions} 改数字即可。
     */
    @Override
    public void vehicleShoot(net.minecraft.world.entity.LivingEntity living, java.util.UUID uuid, Vec3 targetPos) {
        if (!this.level().isClientSide() && this.tickCount - this.dr$lastShotLogTick >= 5) {
            this.dr$lastShotLogTick = this.tickCount;
            try {
                Vec3 world = this.getShootPos(living, 1f);
                Vec3 cam = living != null ? this.getCameraPos(living, 1f) : Vec3.ZERO;
                Vec3 diff = world.subtract(cam);
                Vec3 dir = this.getShootVec(living, 1f);
                double along = diff.dot(dir);
                double lateral = Math.sqrt(Math.max(0.0, diff.lengthSqr() - along * along));
                com.redabysslucia.dragonrise_reforge.Dragonrise_reforge.LOGGER.info(
                        "[DR-SHOT] 出膛点=({}, {}, {}) 相机=({}, {}, {}) 出膛-相机=({}, {}, {}) 距离={} 方向=({}, {}, {}) 沿炮轴={} 横向偏差={} turretXRot={}",
                        f3(world.x), f3(world.y), f3(world.z),
                        f3(cam.x), f3(cam.y), f3(cam.z),
                        f3(diff.x), f3(diff.y), f3(diff.z), f3(diff.length()),
                        f3(dir.x), f3(dir.y), f3(dir.z),
                        f3(along), f3(lateral), f3(this.getTurretXRot()));
            } catch (Throwable t) {
                com.redabysslucia.dragonrise_reforge.Dragonrise_reforge.LOGGER.info("[DR-SHOT] 诊断打印失败: {}", t.toString());
            }
        }
        super.vehicleShoot(living, uuid, targetPos);
    }

    private static String f3(double v) {
        return String.format(java.util.Locale.ROOT, "%.3f", v);
    }
}
