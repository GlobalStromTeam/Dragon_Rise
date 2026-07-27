package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.gun.AmmoConsumer;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.vehicle.subdata.SeatInfo;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.tools.InventoryTool;
import com.redabysslucia.dragonrise_reforge.config.SupplyStationConfig;
import com.redabysslucia.dragonrise_reforge.config.SupplyStationDataLoader;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
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

    private static final float DEFAULT_SUPPLY_RANGE = 24.0f;
    private static final int DEFAULT_NON_MAGAZINE_FILL = 100;
    private static final int DEFAULT_SUPPLY_INTERVAL = 20;
    private static final int DEFAULT_SUPPLY_TIME = 160;

    private int tickCounter = 0;
    private int chargeTick = 0;
    private int chargeCooldown = 0;
    private int chargingSoundTimer = 0;
    private final Map<UUID, Float> trackedVehicleHealth = new HashMap<>();

    private static final int INTERRUPT_COOLDOWN = 10;
    private static final int CHARGING_SOUND_INTERVAL = 20;

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

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public float getMaxHealth() {
        return 2100;
    }

    public float getMass() {return 1145141919;}

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
            return;
        }

        // 始终施加阻力，防止被推动或无限滑动
        Vec3 vel = this.getDeltaMovement();
        this.setDeltaMovement(vel.x * 0.85, vel.y, vel.z * 0.85);

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

        chargingSoundTimer--;
        if (chargingSoundTimer <= 0) {
            this.level().playSound(null, this.blockPosition(),
                    ModSounds.SUPPLY_STATION_CHARGING.get(), SoundSource.BLOCKS,
                    1.0f, 1.0f);
            chargingSoundTimer = CHARGING_SOUND_INTERVAL;
        }

        if (checkVehicleDamage() || !checkTrackedVehiclesHavePlayer()) {
            cancelCharge();
            return;
        }

        if (progress >= 1f) {
            chargeTick = 0;
            trackedVehicleHealth.clear();
            chargingSoundTimer = 0;
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

    private boolean checkTrackedVehiclesHavePlayer() {
        for (UUID uuid : trackedVehicleHealth.keySet()) {
            GeoVehicleEntity vehicle = findTrackedVehicle(uuid);
            if (vehicle == null) {
                return false;
            }
            if (vehicle.getPassengers().stream().noneMatch(p -> p instanceof Player)) {
                return false;
            }
        }
        return true;
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
        chargingSoundTimer = 0;
        setSupplyProgress(0f);
        chargeCooldown = INTERRUPT_COOLDOWN;
    }

    private void startCharge() {
        List<GeoVehicleEntity> vehicles = findNearbyVehicles();
        boolean anyNeedsAction = false;
        trackedVehicleHealth.clear();
        for (GeoVehicleEntity vehicle : vehicles) {
            boolean needsSupply = vehicleNeedsSupply(vehicle);
            boolean needsHeal = vehicleNeedsHealing(vehicle);
            boolean needsBonus = vehicleNeedsBonusItem(vehicle);
            if (needsSupply || needsHeal || needsBonus) {
                anyNeedsAction = true;
                trackedVehicleHealth.put(vehicle.getUUID(), vehicle.getHealth());
            }
        }

        if (anyNeedsAction) {
            chargeTick = 0;
            setSupplyProgress(0.001f);
        }
    }

    private List<GeoVehicleEntity> findNearbyVehicles() {
        float range = getSupplyRange();
        AABB searchBox = this.getBoundingBox().inflate(range);
        return this.level().getEntitiesOfClass(
                GeoVehicleEntity.class, searchBox,
                v -> v.isAlive()
                    && v.distanceToSqr(this) <= range * range
                    && v.getPassengers().stream().anyMatch(p -> p instanceof Player)
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

                List<AmmoConsumer> consumers = gunData.get(GunProp.AMMO_CONSUMER);
                if (consumers == null || consumers.isEmpty()) continue;

                for (AmmoConsumer consumer : consumers) {
                    if (checkWeaponNeedsSupply(vehicle, gunData, config, vehicleRule, consumer)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean vehicleNeedsHealing(GeoVehicleEntity vehicle) {
        SupplyStationConfig config = SupplyStationDataLoader.getConfig();
        String vehicleId = getVehicleId(vehicle);
        SupplyStationConfig.ResupplyRule vehicleRule = config.getRuleForVehicle(vehicleId);
        float healPercent = vehicleRule.healPercent;
        if (healPercent <= 0f && vehicleRule != config.defaultRule) {
            healPercent = config.defaultRule.healPercent;
        }
        if (healPercent <= 0f) return false;
        return vehicle.getHealth() < vehicle.getMaxHealth();
    }

    private boolean vehicleNeedsBonusItem(GeoVehicleEntity vehicle) {
        SupplyStationConfig config = SupplyStationDataLoader.getConfig();
        String vehicleId = getVehicleId(vehicle);
        SupplyStationConfig.ResupplyRule vehicleRule = config.getRuleForVehicle(vehicleId);
        String bonusItemId = config.getEffectiveBonusItem(vehicleRule);
        if (bonusItemId.isEmpty()) {
            return false;
        }

        ResourceLocation itemLocation = ResourceLocation.tryParse(bonusItemId);
        if (itemLocation == null) {
            return false;
        }

        Item bonusItem = ForgeRegistries.ITEMS.getValue(itemLocation);
        if (bonusItem == null) {
            return false;
        }

        int target = config.getEffectiveBonusItemCount(vehicleRule);
        int current = countBonusItem(vehicle, bonusItem);
        return current < target;
    }

    private int countBonusItem(GeoVehicleEntity vehicle, Item bonusItem) {
        var handlerOpt = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handlerOpt.isEmpty()) {
            return 0;
        }

        IItemHandler handler = handlerOpt.get();
        int total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() == bonusItem) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private boolean checkWeaponNeedsSupply(GeoVehicleEntity vehicle, GunData gunData,
                                            SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule,
                                            AmmoConsumer consumer) {
        String ammoKey = getAmmoKey(consumer);
        if (ammoKey.isEmpty()) return false;

        if (config.isPackageBasedMode(vehicleRule, ammoKey)) {
            SupplyStationConfig.AmmoTypeRule ammoRule = config.getAmmoRule(vehicleRule, ammoKey);
            String customItemId = ammoRule.customItem;
            if (customItemId == null || customItemId.isEmpty()) return false;
            ResourceLocation itemLocation = ResourceLocation.tryParse(customItemId);
            if (itemLocation == null) return false;
            Item customItem = ForgeRegistries.ITEMS.getValue(itemLocation);
            if (customItem == null) return false;
            int current = countBonusItem(vehicle, customItem);
            int target = Math.max(1, ammoRule.customItemCount);
            return current < target;
        }

        if (config.isMagazineMode(vehicleRule, ammoKey) && gunData.get(GunProp.MAGAZINE) > 0) {
            int magazine = gunData.get(GunProp.MAGAZINE);
            int currentAmmo = gunData.ammo.get();
            int backupAmmo = countBackupAmmoForConsumer(vehicle, gunData, consumer);
            return (currentAmmo + backupAmmo) < magazine;
        }

        SupplyStationConfig.AmmoTypeRule ammoRule = config.getAmmoRule(vehicleRule, ammoKey);
        int target = ammoRule.fixedAmount > 0 ? ammoRule.fixedAmount : getNonMagazineFillAmount();
        int currentBackup = countBackupAmmoForConsumer(vehicle, gunData, consumer);
        // 包含已上膛/代发的弹药，避免双倍补给
        boolean isSelected = (consumer == gunData.selectedAmmoConsumer());
        int currentLoaded = isSelected ? gunData.ammo.get() : 0;
        return (currentLoaded + currentBackup) < target;
    }

    private void performSupply() {
        SupplyStationConfig config = SupplyStationDataLoader.getConfig();
        List<GeoVehicleEntity> vehicles = findNearbyVehicles();

        for (GeoVehicleEntity vehicle : vehicles) {
            resupplyVehicle(vehicle);
            healVehicle(vehicle, config);
            supplyBonusItem(vehicle, config);
        }

        this.level().playSound(null, this.blockPosition(),
                ModSounds.SUPPLY_STATION_COMPLETE.get(), SoundSource.BLOCKS,
                1.0f, 1.0f);
    }

    private void healVehicle(GeoVehicleEntity vehicle, SupplyStationConfig config) {
        String vehicleId = getVehicleId(vehicle);
        SupplyStationConfig.ResupplyRule vehicleRule = config.getRuleForVehicle(vehicleId);
        float healPercent = vehicleRule.healPercent;
        if (healPercent <= 0f && vehicleRule != config.defaultRule) {
            healPercent = config.defaultRule.healPercent;
        }
        if (healPercent <= 0f) return;

        float maxHealth = vehicle.getMaxHealth();
        float currentHealth = vehicle.getHealth();
        if (currentHealth >= maxHealth) return;

        float healAmount = maxHealth * healPercent / 100f;
        float newHealth = Math.min(maxHealth, currentHealth + healAmount);
        vehicle.setHealth(newHealth);
    }

    private void supplyBonusItem(GeoVehicleEntity vehicle, SupplyStationConfig config) {
        String vehicleId = getVehicleId(vehicle);
        SupplyStationConfig.ResupplyRule vehicleRule = config.getRuleForVehicle(vehicleId);
        String bonusItemId = config.getEffectiveBonusItem(vehicleRule);
        if (bonusItemId.isEmpty()) {
            return;
        }

        ResourceLocation itemLocation = ResourceLocation.tryParse(bonusItemId);
        if (itemLocation == null) {
            return;
        }

        Item bonusItem = ForgeRegistries.ITEMS.getValue(itemLocation);
        if (bonusItem == null) {
            return;
        }

        int target = config.getEffectiveBonusItemCount(vehicleRule);
        int current = countBonusItem(vehicle, bonusItem);
        int toAdd = target - current;
        if (toAdd <= 0) {
            return;
        }

        var handlerOpt = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handlerOpt.isEmpty()) {
            return;
        }

        IItemHandler handler = handlerOpt.get();
        ItemStack stack = new ItemStack(bonusItem, toAdd);
        InventoryTool.insertItem(handler, stack, toAdd);
    }

    private String getVehicleId(GeoVehicleEntity vehicle) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(vehicle.getType());
        return key != null ? key.toString() : "";
    }

    private String getAmmoKey(AmmoConsumer consumer) {
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

    private int countBackupAmmoForConsumer(GeoVehicleEntity vehicle, GunData gunData, AmmoConsumer consumer) {
        var handlerOpt = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handlerOpt.isEmpty()) return 0;
        IItemHandler handler = handlerOpt.get();
        int itemCount = consumer.count(gunData, handler);
        int loadAmount = consumer.getLoadAmount();
        return itemCount * loadAmount;
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

                List<AmmoConsumer> consumers = gunData.get(GunProp.AMMO_CONSUMER);
                if (consumers == null || consumers.isEmpty()) continue;

                for (AmmoConsumer consumer : consumers) {
                    if (resupplyWeapon(vehicle, gunData, config, vehicleRule, globalFallbackFill, consumer)) {
                        anyResupplied = true;
                    }
                }
            }
        }

        return anyResupplied;
    }

    private boolean resupplyWeapon(GeoVehicleEntity vehicle, GunData gunData,
                                    SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule,
                                    int globalFallbackFill, AmmoConsumer consumer) {
        String ammoKey = getAmmoKey(consumer);
        if (ammoKey.isEmpty()) return false;

        if (config.isPackageBasedMode(vehicleRule, ammoKey)) {
            return resupplyPackageWeapon(vehicle, gunData, config.getAmmoRule(vehicleRule, ammoKey));
        }

        if (config.isMagazineMode(vehicleRule, ammoKey)) {
            if (gunData.get(GunProp.MAGAZINE) > 0) {
                return resupplyMagazineWeapon(vehicle, gunData, ammoKey, consumer);
            }
        }

        return resupplyFixedWeapon(vehicle, gunData, config, vehicleRule, ammoKey, globalFallbackFill, consumer);
    }

    private boolean resupplyMagazineWeapon(GeoVehicleEntity vehicle, GunData gunData,
                                            String ammoKey, AmmoConsumer consumer) {
        int magazine = gunData.get(GunProp.MAGAZINE);
        int currentAmmo = gunData.ammo.get();
        int backupAmmo = countBackupAmmoForConsumer(vehicle, gunData, consumer);
        int totalAmmo = currentAmmo + backupAmmo;

        if (totalAmmo >= magazine) {
            return false;
        }

        int ammoNeeded = magazine - totalAmmo;
        if (ammoNeeded <= 0) {
            return false;
        }

        supplyBackupAmmoToVehicle(vehicle, ammoNeeded, consumer);
        return true;
    }

    private boolean resupplyFixedWeapon(GeoVehicleEntity vehicle, GunData gunData,
                                         SupplyStationConfig config, SupplyStationConfig.ResupplyRule vehicleRule,
                                         String ammoKey, int globalFallbackFill, AmmoConsumer consumer) {
        SupplyStationConfig.AmmoTypeRule ammoRule = config.getAmmoRule(vehicleRule, ammoKey);
        int fillAmount = ammoRule.fixedAmount > 0 ? ammoRule.fixedAmount : globalFallbackFill;

        int currentBackup = countBackupAmmoForConsumer(vehicle, gunData, consumer);
        boolean isSelected = (consumer == gunData.selectedAmmoConsumer());
        int currentLoaded = isSelected ? gunData.ammo.get() : 0;
        int totalAmmo = currentBackup + currentLoaded;
        if (totalAmmo >= fillAmount) {
            return false;
        }

        int ammoToAdd = fillAmount - totalAmmo;
        supplyBackupAmmoToVehicle(vehicle, ammoToAdd, consumer);
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

        int target = Math.max(1, ammoRule.customItemCount);
        int current = countBonusItem(vehicle, customItem);
        int toAdd = target - current;
        if (toAdd <= 0) {
            return false;
        }

        var handlerOpt = vehicle.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
        if (handlerOpt.isEmpty()) {
            return false;
        }

        IItemHandler handler = handlerOpt.get();
        ItemStack stack = new ItemStack(customItem, toAdd);
        int inserted = InventoryTool.insertItem(handler, stack, toAdd);
        if (inserted < toAdd) {
            int remaining = toAdd - inserted;
            stack.setCount(remaining);
            InventoryTool.insertItem(handler, stack, remaining);
        }

        return inserted > 0;
    }

    private void supplyBackupAmmoToVehicle(GeoVehicleEntity vehicle, int ammoAmount, AmmoConsumer consumer) {
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
