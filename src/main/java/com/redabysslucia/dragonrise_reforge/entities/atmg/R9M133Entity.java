package com.redabysslucia.dragonrise_reforge.entities.atmg;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.tools.FormatTool;
import com.atsuishio.superbwarfare.tools.ParticleTool;
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

public class R9M133Entity extends VehicleEntity {

    /** 状态机：无弹无筒 */
    public static final int ROUND_EMPTY = 0;
    /** 状态机：发射筒已装填（筒 + 弹都在） */
    public static final int ROUND_LOADED = 1;
    /**
     * 状态机：弹已打光、空筒还挂在架上（纯外观状态）。
     * <p>
     * 交互与官方 TOW 一致：手持弹药右键 = 直接装填（新的一发连同发射筒把空筒替换掉，无需先手动卸筒）；
     * 空手或手持其它物品右键 = 上车。
     */
    public static final int ROUND_SPENT_TUBE = 2;

    // 是否已装填弹药（兼容旧存档/旧逻辑，实际以 ROUND_STATE 为准）
    public static final EntityDataAccessor<Boolean> LOADED = SynchedEntityData.defineId(com.redabysslucia.dragonrise_reforge.entities.atmg.R9M133Entity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> RELOAD_COOLDOWN = SynchedEntityData.defineId(com.redabysslucia.dragonrise_reforge.entities.atmg.R9M133Entity.class, EntityDataSerializers.INT);
    /** 发射筒状态机：ROUND_EMPTY / ROUND_LOADED / ROUND_SPENT_TUBE */
    public static final EntityDataAccessor<Integer> ROUND_STATE = SynchedEntityData.defineId(com.redabysslucia.dragonrise_reforge.entities.atmg.R9M133Entity.class, EntityDataSerializers.INT);

    /** 当前发射筒状态（客户端渲染器也用这个决定 move_missile 是否显示） */
    public int roundState() {
        return this.entityData.get(ROUND_STATE);
    }

    /** 状态机唯一入口：同时维护 LOADED 兼容标记 */
    public void setRoundState(int state) {
        this.entityData.set(ROUND_STATE, state);
        this.entityData.set(LOADED, state == ROUND_LOADED);
    }

    public R9M133Entity(EntityType<R9M133Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LOADED, false);
        this.entityData.define(RELOAD_COOLDOWN, 0);
        this.entityData.define(ROUND_STATE, ROUND_EMPTY);
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

        // 与官方 TOW 完全一致的四条分支：
        // 1) 弹匣里有弹 → 上车
        if (gunData.hasEnoughAmmoToShoot(player)) {
            if (roundState() != ROUND_LOADED) {
                setRoundState(ROUND_LOADED);
            }
            return super.interact(player, hand);
        }

        // 2) 弹匣是空的却残留着「已装填」（旧版本卡死留下的状态）→ 回到空状态，否则永远装不进去
        if (roundState() == ROUND_LOADED) {
            setRoundState(ROUND_EMPTY);
        }

        // 3) 手上不是本发射器使用的弹药（空手 / 别的物品）→ 上车（空筒还在架上也一样）
        if (!gunData.selectedAmmoConsumer().isAmmoItem(player.getMainHandItem())) {
            return super.interact(player, hand);
        }

        // 客户端只负责表现，真正的装填在服务端
        if (!(level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // 4) 手持该弹药 → 装填；冷却中只提示，且与官方 TOW 一致：这一支不再放行上车
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

        // 只有弹匣真的进了弹才算装填成功；空筒状态在这里直接被新的一发取代（不需要先手动卸筒）
        if (gunData.ammo.get() > magazineBefore) {
            setRoundState(ROUND_LOADED);
            serverLevel.playSound(null, getOnPos(), ModSounds.TYPE_63_RELOAD.get(), SoundSource.PLAYERS, 1f, random.nextFloat() * 0.1f + 0.9f);
            return InteractionResult.SUCCESS;
        }

        // 真的没弹：明确提示，保持当前状态，并放行原版交互（仍可上车、可反复尝试）
        player.displayClientMessage(Component.literal("§c没有可用弹药（手持该弹药右键，或先把弹药放进发射器弹仓）"), true);
        return super.interact(player, hand);
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
        list.add(new ItemStack(ModItems.R9M133_DEPLOYER.get()));

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
        // 短号：打完留一个空发射筒挂在架上（外观状态）；之后手持弹药右键即直接装填新的一发（含新筒）
        setRoundState(ROUND_SPENT_TUBE);

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