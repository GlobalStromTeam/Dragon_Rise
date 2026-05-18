package com.redabysslucia.dragonrise_reforge.entities.utils;

import com.atsuishio.superbwarfare.data.gun.AmmoConsumer;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.vehicle.subdata.SeatInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.tools.InventoryTool;
import com.redabysslucia.dragonrise_reforge.config.SupplyStationConfig;
import com.redabysslucia.dragonrise_reforge.config.SupplyStationDataLoader;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AmmoSupplyStationEntity extends GeoVehicleEntity {

    private static final EntityDataAccessor<Float> SUPPLY_RANGE =
            SynchedEntityData.defineId(AmmoSupplyStationEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> NON_MAGAZINE_FILL_AMOUNT =
            SynchedEntityData.defineId(AmmoSupplyStationEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SUPPLY_INTERVAL =
            SynchedEntityData.defineId(AmmoSupplyStationEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SUPPLY_TIME =
            SynchedEntityData.defineId(AmmoSupplyStationEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(AmmoSupplyStationEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SUPPLY_PROGRESS =
            SynchedEntityData.defineId(AmmoSupplyStationEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_SUPPLY_RANGE = 10.0f;
    private static final int DEFAULT_NON_MAGAZINE_FILL = 100;
    private static final int DEFAULT_SUPPLY_INTERVAL = 20;
    private static final int DEFAULT_SUPPLY_TIME = 160;

    private int tickCounter = 0;
    private int activeParticleTick = 0;
    private int chargeTick = 0;
    private int chargeCooldown = 0;
    private final Map<UUID, Float> trackedVehicleHealth = new HashMap<>();

    private static final int INTERRUPT_COOLDOWN = 10;

    public AmmoSupplyStationEntity(EntityType<? extends AmmoSupplyStationEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SUPPLY_RANGE, DEFAULT_SUPPLY_RANGE);
        this.entityData.define(NON_MAGAZINE_FILL_AMOUNT, DEFAULT_NON_MAGAZINE_FILL);
        this.entityData.define(SUPPLY_INTERVAL, DEFAULT_SUPPLY_INTERVAL);
        this.entityData.define(SUPPLY_TIME, DEFAULT_SUPPLY_TIME);
        this.entityData.define(ACTIVE, true);
        this.entityData.define(SUPPLY_PROGRESS, 0f);
    }

    public float getSupplyRange() {
        return this.entityData.get(SUPPLY_RANGE);
    }

    public void setSupplyRange(float range) {
        this.entityData.set(SUPPLY_RANGE, Math.max(1.0f, range));
    }

    public int getNonMagazineFillAmount() {
        return this.entityData.get(NON_MAGAZINE_FILL_AMOUNT);
    }

    public void setNonMagazineFillAmount(int amount) {
        this.entityData.set(NON_MAGAZINE_FILL_AMOUNT, Math.max(1, amount));
    }

    public int getSupplyInterval() {
        return this.entityData.get(SUPPLY_INTERVAL);
    }

    public void setSupplyInterval(int interval) {
        this.entityData.set(SUPPLY_INTERVAL, Math.max(1, interval));
    }

    public int getSupplyTime() {
        return this.entityData.get(SUPPLY_TIME);
    }

    public void setSupplyTime(int time) {
        this.entityData.set(SUPPLY_TIME, Math.max(1, time));
    }

    public boolean isActive() {
        return this.entityData.get(ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(ACTIVE, active);
    }

    public float getSupplyProgress() {
        return this.entityData.get(SUPPLY_PROGRESS);
    }

    private void setSupplyProgress(float progress) {
        this.entityData.set(SUPPLY_PROGRESS, Math.max(0f, Math.min(1f, progress)));
    }

    public boolean isCharging() {
        return getSupplyProgress() > 0f && getSupplyProgress() < 1f;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (isCharging()) {
                spawnChargeParticles();
            } else if (isActive()) {
                spawnIdleParticles();
            }
            return;
        }

        if (!isActive()) {
            return;
        }

        if (isCharging()) {
            tickCharge();
            return;
        }

        if (chargeCooldown > 0) {
            chargeCooldown--;
            return;
        }

        tickCounter++;
        int interval = getSupplyInterval();
        if (interval <= 0) interval = DEFAULT_SUPPLY_INTERVAL;

        if (tickCounter >= interval) {
            tickCounter = 0;
            startCharge();
        }
    }

    private void tickCharge() {
        int totalTime = getSupplyTime();
        if (totalTime <= 0) totalTime = DEFAULT_SUPPLY_TIME;

        chargeTick++;
        float progress = Math.min(1f, (float) chargeTick / (float) totalTime);
        setSupplyProgress(progress);

        if (checkVehicleDamage()) {
            cancelCharge();
            return;
        }

        if (progress >= 1f) {
            chargeTick = 0;
            trackedVehicleHealth.clear();
            performSupply();
            setSupplyProgress(0f);
        }
    }

    private boolean checkVehicleDamage() {
        for (Map.Entry<UUID, Float> entry : trackedVehicleHealth.entrySet()) {
            GeoVehicleEntity vehicle = findTrackedVehicle(entry.getKey());
            if (vehicle == null || !vehicle.isAlive()) {
                return true;
            }
            if (vehicle.getHealth() < entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    private GeoVehicleEntity findTrackedVehicle(UUID uuid) {
        float range = getSupplyRange();
        AABB searchBox = this.getBoundingBox().inflate(range);
        List<GeoVehicleEntity> vehicles = this.level().getEntitiesOfClass(
                GeoVehicleEntity.class, searchBox,
                v -> v.isAlive() && v.getUUID().equals(uuid)
        );
        return vehicles.isEmpty() ? null : vehicles.get(0);
    }

    private void cancelCharge() {
        chargeTick = 0;
        trackedVehicleHealth.clear();
        setSupplyProgress(0f);
        chargeCooldown = INTERRUPT_COOLDOWN;
    }

    private void startCharge() {
        List<GeoVehicleEntity> vehicles = findNearbyVehicles();
        boolean anyNeedsSupply = false;
        trackedVehicleHealth.clear();
        for (GeoVehicleEntity vehicle : vehicles) {
            if (vehicleNeedsSupply(vehicle)) {
                anyNeedsSupply = true;
                trackedVehicleHealth.put(vehicle.getUUID(), vehicle.getHealth());
            }
        }

        if (anyNeedsSupply) {
            chargeTick = 0;
            setSupplyProgress(0.001f);
        }
    }

    private List<GeoVehicleEntity> findNearbyVehicles() {
        float range = getSupplyRange();
        AABB searchBox = this.getBoundingBox().inflate(range);
        return this.level().getEntitiesOfClass(
                GeoVehicleEntity.class, searchBox,
                v -> v.isAlive() && v.distanceToSqr(this) <= range * range
        );
    }

    private boolean vehicleNeedsSupply(GeoVehicleEntity vehicle) {
        SupplyStationConfig config = SupplyStationDataLoader.getConfig();
        String vehicleId = getVehicleId(vehicle);
        SupplyStationConfig.ResupplyRule vehicleRule = config.getRuleForVehicle(vehicleId);

        for (int seat = 0; seat < vehicle.getMaxPassengers(); seat++) {
            SeatInfo seatInfo = vehicle.getSeat(seat);
            if (seatInfo == null) continue;

            List<String> weaponNames = seatInfo.weapons();
            if (weaponNames == null || weaponNames.isEmpty()) continue;

            for (int weaponIdx = 0; weaponIdx < weaponNames.size(); weaponIdx++) {
                GunData gunData = vehicle.getGunData(seat, weaponIdx);
                if (gunData == null) continue;
                if (gunData.hasInfiniteBackupAmmo(vehicle)) continue;

                boolean needs = checkWeaponNeedsSupply(vehicle, gunData, config, vehicleRule);
                if (needs) return true;
            }
        }
        return false;
    }

    private boolean checkWeaponNeedsSupply(GeoVehicleEntity vehicle, GunData gunData,
                                            SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule) {
        String ammoKey = getAmmoKey(gunData);

        if (config.isPackageBasedMode(vehicleRule, ammoKey)) {
            return true;
        }

        if (config.isMagazineMode(vehicleRule, ammoKey) && gunData.get(GunProp.MAGAZINE) > 0) {
            int magazine = gunData.get(GunProp.MAGAZINE);
            int currentAmmo = gunData.ammo.get();
            int backupAmmo = gunData.countBackupAmmo(vehicle);
            return (currentAmmo + backupAmmo) < magazine;
        }

        SupplyStationConfig.AmmoTypeRule ammoRule = config.getAmmoRule(vehicleRule, ammoKey);
        int target = ammoRule.fixedAmount > 0 ? ammoRule.fixedAmount : getNonMagazineFillAmount();
        int currentBackup = gunData.countBackupAmmo(vehicle);
        return currentBackup < target;
    }

    private void performSupply() {
        List<GeoVehicleEntity> vehicles = findNearbyVehicles();
        boolean anyResupplied = false;

        for (GeoVehicleEntity vehicle : vehicles) {
            if (resupplyVehicle(vehicle)) {
                anyResupplied = true;
            }
        }

        if (anyResupplied) {
            spawnSupplyParticles(vehicles);
        }
    }

    private String getVehicleId(GeoVehicleEntity vehicle) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(vehicle.getType());
        return key != null ? key.toString() : "";
    }

    private String getAmmoKey(GunData gunData) {
        AmmoConsumer consumer = gunData.selectedAmmoConsumer();
        if (consumer == null) {
            return "";
        }
        ItemStack stack = consumer.stack();
        if (stack.isEmpty()) {
            return "";
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null ? key.toString() : "";
    }

    private boolean resupplyVehicle(GeoVehicleEntity vehicle) {
        SupplyStationConfig config = SupplyStationDataLoader.getConfig();
        String vehicleId = getVehicleId(vehicle);
        SupplyStationConfig.ResupplyRule vehicleRule = config.getRuleForVehicle(vehicleId);

        int globalFallbackFill = getNonMagazineFillAmount();
        boolean anyResupplied = false;

        for (int seat = 0; seat < vehicle.getMaxPassengers(); seat++) {
            SeatInfo seatInfo = vehicle.getSeat(seat);
            if (seatInfo == null) {
                continue;
            }

            List<String> weaponNames = seatInfo.weapons();
            if (weaponNames == null || weaponNames.isEmpty()) {
                continue;
            }

            for (int weaponIdx = 0; weaponIdx < weaponNames.size(); weaponIdx++) {
                GunData gunData = vehicle.getGunData(seat, weaponIdx);
                if (gunData == null) {
                    continue;
                }

                if (gunData.hasInfiniteBackupAmmo(vehicle)) {
                    continue;
                }

                if (resupplyWeapon(vehicle, gunData, seat, weaponIdx, config, vehicleRule, globalFallbackFill)) {
                    anyResupplied = true;
                }
            }
        }

        return anyResupplied;
    }

    private boolean resupplyWeapon(GeoVehicleEntity vehicle, GunData gunData, int seat, int weaponIdx,
                                    SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule,
                                    int globalFallbackFill) {
        String ammoKey = getAmmoKey(gunData);

        if (config.isPackageBasedMode(vehicleRule, ammoKey)) {
            return resupplyPackageWeapon(vehicle, gunData, config.getAmmoRule(vehicleRule, ammoKey));
        }

        if (config.isMagazineMode(vehicleRule, ammoKey)) {
            if (gunData.get(GunProp.MAGAZINE) > 0) {
                return resupplyMagazineWeapon(vehicle, gunData, config, vehicleRule, ammoKey);
            }
        }

        return resupplyFixedWeapon(vehicle, gunData, config, vehicleRule, ammoKey, globalFallbackFill);
    }

    private boolean resupplyMagazineWeapon(GeoVehicleEntity vehicle, GunData gunData,
                                            SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule,
                                            String ammoKey) {
        int magazine = gunData.get(GunProp.MAGAZINE);
        int currentAmmo = gunData.ammo.get();
        int backupAmmo = gunData.countBackupAmmo(vehicle);
        int totalAmmo = currentAmmo + backupAmmo;

        if (totalAmmo >= magazine) {
            return false;
        }

        int ammoNeeded = magazine - totalAmmo;
        if (ammoNeeded <= 0) {
            return false;
        }

        supplyBackupAmmoToVehicle(vehicle, gunData, ammoNeeded);
        return true;
    }

    private boolean resupplyFixedWeapon(GeoVehicleEntity vehicle, GunData gunData,
                                         SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule,
                                         String ammoKey, int globalFallbackFill) {
        SupplyStationConfig.AmmoTypeRule ammoRule = config.getAmmoRule(vehicleRule, ammoKey);
        int fillAmount = ammoRule.fixedAmount > 0 ? ammoRule.fixedAmount : globalFallbackFill;

        int currentBackup = gunData.countBackupAmmo(vehicle);
        if (currentBackup >= fillAmount) {
            return false;
        }

        int ammoToAdd = fillAmount - currentBackup;
        supplyBackupAmmoToVehicle(vehicle, gunData, ammoToAdd);
        return true;
    }

    private boolean resupplyPackageWeapon(GeoVehicleEntity vehicle, GunData gunData,
                                           SupplyStationConfig.AmmoTypeRule ammoRule) {
        String customItemId = ammoRule.customItem;
        if (customItemId == null || customItemId.isEmpty()) {
            return false;
        }

        ResourceLocation itemLocation = ResourceLocation.tryParse(customItemId);
        if (itemLocation == null) {
            return false;
        }

        Item customItem = ForgeRegistries.ITEMS.getValue(itemLocation);
        if (customItem == null) {
            return false;
        }

        int count = Math.max(1, ammoRule.customItemCount);

        var handlerOpt = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handlerOpt.isEmpty()) {
            return false;
        }

        IItemHandler handler = handlerOpt.get();
        ItemStack stack = new ItemStack(customItem, count);
        int inserted = InventoryTool.insertItem(handler, stack, count);
        if (inserted < count) {
            int remaining = count - inserted;
            stack.setCount(remaining);
            InventoryTool.insertItem(handler, stack, remaining);
        }

        return inserted > 0;
    }

    private void supplyBackupAmmoToVehicle(GeoVehicleEntity vehicle, GunData gunData, int ammoAmount) {
        AmmoConsumer consumer = gunData.selectedAmmoConsumer();
        if (consumer == null) {
            return;
        }

        int loadAmount = consumer.getLoadAmount();
        if (loadAmount <= 0) {
            loadAmount = 1;
        }

        int itemsNeeded = (ammoAmount + loadAmount - 1) / loadAmount;
        if (itemsNeeded <= 0) {
            return;
        }

        var handlerOpt = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handlerOpt.isEmpty()) {
            return;
        }

        IItemHandler handler = handlerOpt.get();
        ItemStack ammoStack = consumer.stack().copy();
        if (ammoStack.isEmpty()) {
            return;
        }

        int inserted = InventoryTool.insertItem(handler, ammoStack, itemsNeeded);
        if (inserted < itemsNeeded) {
            int remaining = itemsNeeded - inserted;
            ammoStack.setCount(remaining);
            InventoryTool.insertItem(handler, ammoStack, remaining);
        }
    }

    private void spawnChargeParticles() {
        activeParticleTick++;
        if (activeParticleTick % 4 != 0) return;

        Vec3 pos = this.position();
        double y = pos.y + 1.0;
        double r = 0.6;
        double angle = (activeParticleTick * 0.3) % (Math.PI * 2);
        double ox = Math.cos(angle) * r;
        double oz = Math.sin(angle) * r;
        this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                pos.x + ox, y, pos.z + oz, 0, 0.05, 0);
    }

    private void spawnSupplyParticles(List<GeoVehicleEntity> vehicles) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 stationPos = this.position();
        for (GeoVehicleEntity vehicle : vehicles) {
            Vec3 vehiclePos = vehicle.position();
            Vec3 midPoint = stationPos.add(vehiclePos).scale(0.5);

            for (int i = 0; i < 8; i++) {
                double dx = (this.random.nextDouble() - 0.5) * 0.5;
                double dy = this.random.nextDouble() * 1.5;
                double dz = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        midPoint.x + dx, midPoint.y + dy, midPoint.z + dz,
                        1, 0, 0, 0, 0.1);
            }
        }
    }

    private void spawnIdleParticles() {
        activeParticleTick++;
        if (activeParticleTick % 10 != 0) return;

        Vec3 pos = this.position();
        double y = pos.y + 1.0;
        for (int i = 0; i < 2; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 0.8;
            double oz = (this.random.nextDouble() - 0.5) * 0.8;
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + ox, y, pos.z + oz, 0, 0.05, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SupplyRange")) {
            setSupplyRange(compound.getFloat("SupplyRange"));
        }
        if (compound.contains("NonMagazineFillAmount")) {
            setNonMagazineFillAmount(compound.getInt("NonMagazineFillAmount"));
        }
        if (compound.contains("SupplyInterval")) {
            setSupplyInterval(compound.getInt("SupplyInterval"));
        }
        if (compound.contains("SupplyTime")) {
            setSupplyTime(compound.getInt("SupplyTime"));
        }
        if (compound.contains("Active")) {
            setActive(compound.getBoolean("Active"));
        }
        if (compound.contains("TickCounter")) {
            tickCounter = compound.getInt("TickCounter");
        }
        if (compound.contains("ChargeTick")) {
            chargeTick = compound.getInt("ChargeTick");
        }
        if (compound.contains("ChargeCooldown")) {
            chargeCooldown = compound.getInt("ChargeCooldown");
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SupplyRange", getSupplyRange());
        compound.putInt("NonMagazineFillAmount", getNonMagazineFillAmount());
        compound.putInt("SupplyInterval", getSupplyInterval());
        compound.putInt("SupplyTime", getSupplyTime());
        compound.putBoolean("Active", isActive());
        compound.putInt("TickCounter", tickCounter);
        compound.putInt("ChargeTick", chargeTick);
        compound.putInt("ChargeCooldown", chargeCooldown);
    }
}
