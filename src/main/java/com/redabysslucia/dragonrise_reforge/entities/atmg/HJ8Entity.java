package com.redabysslucia.dragonrise_reforge.entities.atmg;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.entities.projectile.MissileShellEntity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HJ8Entity extends VehicleEntity {

    /** 状态机：无弹无筒 */
    public static final int ROUND_EMPTY = 0;
    /** 状态机：发射筒已装填（筒 + 弹都在） */
    public static final int ROUND_LOADED = 1;
    /** 状态机：发射筒里的弹已打光、空筒还挂在架上（仅短号 9M133 使用，红箭8 是自动抛弃） */
    public static final int ROUND_SPENT_TUBE = 2;

    // 是否已装填弹药（兼容旧存档/旧逻辑，实际以 ROUND_STATE 为准）
    public static final EntityDataAccessor<Boolean> LOADED = SynchedEntityData.defineId(com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> RELOAD_COOLDOWN = SynchedEntityData.defineId(com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity.class, EntityDataSerializers.INT);
    /** 发射筒状态机：ROUND_EMPTY / ROUND_LOADED / ROUND_SPENT_TUBE */
    public static final EntityDataAccessor<Integer> ROUND_STATE = SynchedEntityData.defineId(com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity.class, EntityDataSerializers.INT);

    public HJ8Entity(EntityType<com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LOADED, false);
        this.entityData.define(RELOAD_COOLDOWN, 0);
        this.entityData.define(ROUND_STATE, ROUND_EMPTY);
    }

    /** 当前发射筒状态（客户端渲染器也用这个决定 move_missile 是否显示） */
    public int roundState() {
        return this.entityData.get(ROUND_STATE);
    }

    /** 状态机唯一入口：同时维护 LOADED 兼容标记 */
    public void setRoundState(int state) {
        this.entityData.set(ROUND_STATE, state);
        this.entityData.set(LOADED, state == ROUND_LOADED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("State", this.entityData.get(LOADED));
        compound.putInt("RoundState", roundState());
        compound.putInt("ReloadCoolDown", this.entityData.get(RELOAD_COOLDOWN));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // 旧存档没有 RoundState，用旧的 State 布尔量推导
        int state = compound.contains("RoundState")
                ? compound.getInt("RoundState")
                : (compound.getBoolean("State") ? ROUND_LOADED : ROUND_EMPTY);
        setRoundState(state);
        this.entityData.set(RELOAD_COOLDOWN, compound.getInt("ReloadCoolDown"));
    }

    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
        var gunData = getGunData(0);
        if (gunData == null) return InteractionResult.SUCCESS;

        int coolDown = (int) Math.ceil(20f / ((float) vehicleWeaponRpm(0) / 60));

        // 弹匣里已经有弹 → 确保状态机是「已装填」，放行原版交互（上车）
        if (gunData.hasEnoughAmmoToShoot(player)) {
            if (roundState() != ROUND_LOADED) {
                setRoundState(ROUND_LOADED);
            }
            return super.interact(player, hand);
        }

        // 弹匣是空的却残留着「已装填」（旧版本卡死留下的状态）→ 回到空状态，否则永远装不进去
        if (roundState() == ROUND_LOADED) {
            setRoundState(ROUND_EMPTY);
        }

        // 手里不是本发射器使用的弹药 → 放行原版交互（上车）
        if (!gunData.selectedAmmoConsumer().isAmmoItem(player.getMainHandItem())) {
            return super.interact(player, hand);
        }

        // 客户端只负责表现，真正的装填在服务端
        if (!(level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // 冷却中：只提示冷却时间，且与官方 TOW 一致 —— 手持弹药时这一支不放行上车
        if (entityData.get(RELOAD_COOLDOWN) > 0) {
            player.displayClientMessage(Component.literal(FormatTool.format1DZ((double) (coolDown - entityData.get(RELOAD_COOLDOWN)) / 20) + " / " + FormatTool.format1DZ((double) coolDown / 20)), true);
            return InteractionResult.SUCCESS;
        }

        // 取弹来源：优先发射器自身弹仓（Espetro 固定武器工事就是把导弹放进这个容器），
        // 其次玩家背包（手持弹药右键，与官方 TOW 同款玩法）。
        int magazineBefore = gunData.ammo.get();
        var container = getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
        boolean fromContainer = container != null && gunData.countBackupAmmo(container) > 0;

        // 换来源前必须清掉缓存：countBackupAmmo(Entity) 的缓存不区分来源，会读到另一个容器的数量
        gunData.cachedBackupAmmo = -1;
        modifyGunData(0, data -> data.reloadAmmo(fromContainer ? this : player));

        // 只有弹匣真的进了弹才算装填成功
        if (gunData.ammo.get() > magazineBefore) {
            setRoundState(ROUND_LOADED);
            serverLevel.playSound(null, getOnPos(), ModSounds.TYPE_63_RELOAD.get(), SoundSource.PLAYERS, 1f, random.nextFloat() * 0.1f + 0.9f);
            return InteractionResult.SUCCESS;
        }

        // 真的没弹：明确提示，不放行状态机（保持 EMPTY），并放行原版交互（仍可上车、可反复尝试）
        player.displayClientMessage(Component.literal("§c没有可用弹药（手持该弹药右键，或先把弹药放进发射器弹仓）"), true);
        return super.interact(player, hand);
    }

    /**
     * 从炮管尾部向后弹出一个空发射筒实体（纯表现，{@link MissileShellEntity#LIFETIME_TICKS} tick 后自行消失）。
     * 只在服务端调用：{@code vehicleShoot} 本身就在服务端执行。
     */
    private void ejectMissileShell(LivingEntity shooter) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var shellType = ModEntities.MISSILE_SHELL.get();
        var shell = new MissileShellEntity(shellType, serverLevel);

        var back = getBarrelVector(1).scale(-1);                       // 炮管反方向
        var spawnPos = getShootPos(shooter, 1).add(back.scale(0.75));  // 从炮口稍后一点弹出
        var velocity = back.scale(0.55).add(0, 0.18, 0);               // 向后弹出 + 少量上抛

        shell.eject(spawnPos, velocity);
        serverLevel.addFreshEntity(shell);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (entityData.get(RELOAD_COOLDOWN) > 0) {
            entityData.set(RELOAD_COOLDOWN, entityData.get(RELOAD_COOLDOWN) - 1);
        }

        // 状态机对账：弹匣里确实有弹，就一定是「已装填」（防止存档/指令等外部改动造成脱节）
        if (!level().isClientSide && roundState() != ROUND_LOADED) {
            var data = getGunData(0);
            if (data != null && data.ammo.get() > 0) {
                setRoundState(ROUND_LOADED);
            }
        }
    }

    @Override
    public @NotNull List<ItemStack> getRetrieveItems() {
        var list = new ArrayList<ItemStack>();
        list.add(new ItemStack(ModItems.HJ8_DEPLOYER.get()));

        var data = getGunData(0);
        if (roundState() == ROUND_LOADED && data != null) {
            var stack = data.selectedAmmoConsumer().stack().copyWithCount(data.withdrawAmmoCount());
            if (!stack.isEmpty()) {
                list.add(stack.copy());
            }
        }

        return list;
    }

    /*
     * 卓越前线的 VehicleEntity 有三个 vehicleShoot 重载，各自独立实现（都会自己开火，互不委托）：
     *   (living, weaponName, targetPos) / (living, weaponName, uuid, targetPos) / (living, uuid, targetPos)
     * 玩家开火走的是带 weaponName 的那个，AI 走 uuid 那个 —— 所以三个都要覆写，否则状态机不会被触发。
     */
    @Override
    public void vehicleShoot(LivingEntity living, String weaponName, Vec3 targetPos) {
        super.vehicleShoot(living, weaponName, targetPos);
        onFired(living);
    }

    @Override
    public void vehicleShoot(LivingEntity living, String weaponName, UUID uuid, Vec3 targetPos) {
        super.vehicleShoot(living, weaponName, uuid, targetPos);
        onFired(living);
    }

    @Override
    public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
        super.vehicleShoot(living, uuid, targetPos);
        onFired(living);
    }

    /** 三种开火入口共用的收尾：状态机 + 冷却 + 尾焰 + 粒子 */
    private void onFired(LivingEntity living) {
        // 红箭8：发射后发射筒自动抛弃 —— 模型上的 move_missile 消失，同时在炮管尾部弹出一个空筒实体（3 秒后消失）
        setRoundState(ROUND_EMPTY);
        ejectMissileShell(living);

        var barrelVector = getBarrelVector(1);
        var pos = getShootPos(living, 1).add(barrelVector.scale(-0.5));
        var ab = new AABB(pos, pos).inflate(0.75).move(barrelVector.scale(-2)).expandTowards(barrelVector.scale(-5));
        int coolDown = (int) Math.ceil(20f / ((float) vehicleWeaponRpm(0) / 60));
        entityData.set(RELOAD_COOLDOWN, coolDown);

        // 尾焰伤害
        for (var entity : level().getEntities(EntityTypeTest.forClass(Entity.class), ab,
                target -> target != this && target != getFirstPassenger() && target.getVehicle() == null)
        ) {
            entity.hurt(ModDamageTypes.causeBurnDamage(entity.level().registryAccess(), living), 30 - 2 * entity.distanceTo(this));
            double force = 4 - 0.7 * entity.distanceTo(this);
            entity.push(-force * barrelVector.x, -force * barrelVector.y, -force * barrelVector.z);
        }

        // 粒子效果
        if (level() instanceof ServerLevel serverLevel) {
            ParticleTool.spawnMediumCannonMuzzleParticles(barrelVector.scale(-1), pos, serverLevel, this);
            ParticleTool.spawnMediumCannonMuzzleParticles(barrelVector, pos, serverLevel, this);
        }
    }
//    @Override
//    public void destroy() {
//        if (this.level() instanceof ServerLevel level) {
//            var x = this.getX();
//            var y = this.getY();
//            var z = this.getZ();
//            level.explode(null, x, y, z, 0, Level.ExplosionInteraction.NONE);
//            ItemEntity mortar = new ItemEntity(level, x, (y + 1), z, new ItemStack(ModItems.MORTAR_BARREL.get()));
//            mortar.setPickUpDelay(10);
//            level.addFreshEntity(mortar);
//        }
//        super.destroy();
//    }
}