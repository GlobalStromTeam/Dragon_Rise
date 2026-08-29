package com.redabysslucia.dragonrise_reforge.entities.special;

import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.atsuishio.superbwarfare.init.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 攻击无人机 —— 侦察无人车（R6DroneEntity）的武装变种。
 * 功能完全一致（移动/跳跃/链接/回收/相机插值等全部继承），唯一区别：
 * 左键发射 superbwarfare:projectile（伤害 5、无下坠、速度很快），冷却 4 秒。
 * 射击输入由客户端 R6DroneControlMessage 的射击位（bit5）驱动，服务端 tick 触发。
 */
public class AttackDroneEntity extends R6DroneEntity {

    /** 射击冷却（tick） */
    private static final int SHOOT_COOLDOWN = 80; // 4 秒
    /** 弹速（格/tick）—— 快速，无下坠 */
    private static final float PROJECTILE_VELOCITY = 30.0f;
    /** 伤害 */
    private static final float PROJECTILE_DAMAGE = 5.0f;
    /** 散布（角度，越小越准） */
    private static final float PROJECTILE_SPREAD = 0.5f;
    /** 弹丸存在时间（tick），0.2 秒 = 4 tick */
    private static final int PROJECTILE_LIFE = 4;

    private int shootCooldown;
    /** 来自 R6DroneControlMessage 的射击输入（左键按住） */
    private boolean firing;

    public AttackDroneEntity(EntityType<? extends AttackDroneEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        // 射击冷却
        if (this.shootCooldown > 0) {
            this.shootCooldown--;
        }

        // 仅遥控时允许开火
        Player controller = this.getController();
        if (controller == null || !this.isMonitorControlling(controller)) {
            this.firing = false;
            return;
        }

        if (this.firing && this.shootCooldown <= 0) {
            this.shoot();
            this.shootCooldown = SHOOT_COOLDOWN;
        }
    }

    /** 发射一发无下坠快速弹（superbwarfare:projectile） */
    private void shoot() {
        Player controller = this.getController();
        if (controller == null) {
            return;
        }
        ProjectileEntity projectile = new ProjectileEntity(ModEntities.PROJECTILE.get(), this.level());
        projectile.setDamage(PROJECTILE_DAMAGE);
        projectile.setVelocity(PROJECTILE_VELOCITY);
        projectile.setCustomGravity(0.0f); // 无下坠
        projectile.setLife(PROJECTILE_LIFE); // 存在 0.2 秒
        projectile.shooter(this);

        // 发射位置：车体中心前方一点、略高于地面
        Vec3 pos = this.position().add(this.getLookAngle().scale(0.5)).add(0.0, 0.1, 0.0);
        projectile.setPos(pos.x, pos.y, pos.z);

        // 方向 = 控制者视角方向（与车身朝向一致）。
        // 用原版 Projectile.shoot(x,y,z,velocity,spread) 方向向量版；
        // 不能用 SBW 的 shoot(LivingEntity,...) 重载传 0,0,0 —— normalize 零向量得 NaN。
        Vec3 dir = controller.getLookAngle();
        projectile.shoot(dir.x, dir.y, dir.z, PROJECTILE_VELOCITY, PROJECTILE_SPREAD);

        this.level().addFreshEntity(projectile);

        // SBW AWM 第一人称消音开火声。
        // 注意：SBW 枪械音效只写在 sounds.json（客户端资源），不在 ForgeRegistries.SOUND_EVENTS，
        // 无法 getValue 拿 SoundEvent —— 直接构造同名 SoundEvent 即可，
        // 客户端按 location 从 sounds.json 解析播放。
        var fireSound = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("superbwarfare", "awm_fire_1p_s"));
        // 经 playDroneSound 遥控者专用包（客户端监听 = 主相机 = 无人机视角，音源在无人车处 → 距离 0 全音量）
        this.playDroneSound(fireSound, 1.0f);
    }

    /** 攻击无人机的部署物品 */
    @Override
    protected net.minecraft.world.item.Item getDeployItem() {
        return com.redabysslucia.dragonrise_reforge.init.ModItems.ATTACK_DRONE.get();
    }

    /** 服务器接收键盘输入：复用父类位标志 + 新增 bit5 = 射击 */
    @Override
    public void processInput(short keys) {
        super.processInput(keys);
        this.firing = (keys & 0b000100000) > 0;
    }
}
