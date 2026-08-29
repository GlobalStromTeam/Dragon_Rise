package com.redabysslucia.dragonrise_reforge.entities;


import net.minecraft.network.syncher.SynchedEntityData;import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.inventory.menu.ChargingStationMenu;
import com.atsuishio.superbwarfare.network.dataslot.ContainerEnergyData;
import com.redabysslucia.dragonrise_reforge.config.server.MiscConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class GeneratorEntity extends VehicleEntity implements MenuProvider {
    private static final int SLOT_FUEL = 0;
    private static final int SLOT_CHARGE = 1;
    private static final int MAX_DATA_COUNT = 4;

    public static final int MAX_ENERGY = MiscConfig.CHARGING_STATION_MAX_ENERGY;
    public static final int DEFAULT_FUEL_TIME = MiscConfig.CHARGING_STATION_DEFAULT_FUEL_TIME;
    public static final int CHARGE_SPEED = MiscConfig.CHARGING_STATION_GENERATE_SPEED;
    public static final int CHARGE_OTHER_SPEED = MiscConfig.CHARGING_STATION_TRANSFER_SPEED;
    public static final int CHARGE_RADIUS = MiscConfig.CHARGING_STATION_CHARGE_RADIUS;

    private final NonNullList<ItemStack> generatorItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY);
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);

    public int fuelTick = 0;
    public int maxFuelTick = DEFAULT_FUEL_TIME;
    public boolean showRange = false;

    protected final ContainerEnergyData dataAccess = new ContainerEnergyData() {
        @Override
        public long get(int index) {
            return switch (index) {
                case 0 -> GeneratorEntity.this.fuelTick;
                case 1 -> GeneratorEntity.this.maxFuelTick;
                case 2 -> GeneratorEntity.this.energyStorage.getEnergyStored();
                case 3 -> GeneratorEntity.this.showRange ? 1L : 0L;
                default -> 0L;
            };
        }

        @Override
        public void set(int index, long value) {
            switch (index) {
                case 0 -> GeneratorEntity.this.fuelTick = (int) value;
                case 1 -> GeneratorEntity.this.maxFuelTick = (int) value;
                case 2 -> GeneratorEntity.this.energyStorage.receiveEnergy((int) value, false);
                case 3 -> GeneratorEntity.this.showRange = value == 1L;
            }
        }

        @Override
        public int getCount() {
            return MAX_DATA_COUNT;
        }
    };

    public GeneratorEntity(EntityType<? extends GeneratorEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
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

    public float getMass() {
        return 1145141919;
    }

    private void chargeEntity(EnergyStorage handler) {
        if (this.level() == null) return;
        if (this.level().getGameTime() % 20 != 0L) return;

        AABB searchBox = this.getBoundingBox().inflate(CHARGE_RADIUS);
        this.level().getEntitiesOfClass(Entity.class, searchBox).forEach(entity -> {
            IEnergyStorage cap = entity.getCapability(Capabilities.EnergyStorage.ENTITY, null);
            if (cap == null) return;
            if (cap.canReceive()) {
                int charged = cap.receiveEnergy(Math.min(handler.getEnergyStored(), CHARGE_OTHER_SPEED * 20), false);
                handler.extractEnergy(charged, false);
            }
        });
        this.setChanged();
    }

    private void chargeItemStack(EnergyStorage handler) {
        ItemStack stack = generatorItems.get(SLOT_CHARGE);
        if (stack.isEmpty()) return;

        IEnergyStorage cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (cap == null) return;
        if (cap.getEnergyStored() < cap.getMaxEnergyStored()) {
            int charged = cap.receiveEnergy(Math.min(CHARGE_OTHER_SPEED, handler.getEnergyStored()), false);
            handler.extractEnergy(Math.min(charged, handler.getEnergyStored()), false);
        }
        this.setChanged();
    }

    private void chargeBlock(EnergyStorage handler) {
        if (this.level() == null) return;

        BlockPos pos = new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
        for (Direction dir : Direction.values()) {
            BlockPos target = pos.relative(dir);
            BlockEntity be = this.level().getBlockEntity(target);
            if (be == null) {
                continue;
            }
            IEnergyStorage cap = this.level().getCapability(Capabilities.EnergyStorage.BLOCK, target,
                    be.getBlockState(), be, dir);
            if (cap == null) {
                continue;
            }
            if (cap.canReceive() && cap.getEnergyStored() < cap.getMaxEnergyStored()) {
                int received = cap.receiveEnergy(Math.min(handler.getEnergyStored(), CHARGE_OTHER_SPEED), false);
                handler.extractEnergy(received, false);
                be.setChanged();
                this.setChanged();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.85));

        EnergyStorage handler = this.energyStorage;
        if (handler.getEnergyStored() > 0) {
            chargeEntity(handler);
        }
        if (handler.getEnergyStored() > 0) {
            chargeItemStack(handler);
        }
        if (handler.getEnergyStored() > 0) {
            chargeBlock(handler);
        }

        if (fuelTick > 0) {
            fuelTick--;
            if (handler.getEnergyStored() < handler.getMaxEnergyStored()) {
                handler.receiveEnergy(CHARGE_SPEED, false);
            }
        } else if (!generatorItems.get(SLOT_FUEL).isEmpty()) {
            if (handler.getEnergyStored() >= handler.getMaxEnergyStored()) return;

            ItemStack fuel = generatorItems.get(SLOT_FUEL);
            int burnTime = fuel.getBurnTime(RecipeType.SMELTING);

            IEnergyStorage itemEnergy = fuel.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemEnergy != null) {
                int toExtract = Math.min(CHARGE_OTHER_SPEED, handler.getMaxEnergyStored() - handler.getEnergyStored());
                if (itemEnergy.canExtract() && handler.canReceive()) {
                    handler.receiveEnergy(itemEnergy.extractEnergy(toExtract, false), false);
                }
                this.setChanged();
            } else if (burnTime > 0) {
                fuelTick = burnTime;
                maxFuelTick = burnTime;

                if (fuel.hasCraftingRemainingItem()) {
                    if (fuel.getCount() <= 1) {
                        generatorItems.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                    } else {
                        ItemStack copy = fuel.getCraftingRemainingItem().copy();
                        copy.setCount(1);
                        ItemEntity itemEntity = new ItemEntity(this.level(),
                                this.getX() + 0.5, this.getY() + 0.2, this.getZ() + 0.5, copy);
                        this.level().addFreshEntity(itemEntity);
                        fuel.shrink(1);
                    }
                } else {
                    fuel.shrink(1);
                }
                this.setChanged();
            } else {
                var food = fuel.get(net.minecraft.core.component.DataComponents.FOOD);
                if (food != null) {
                    int nutrition = food.nutrition();
                    float saturation = food.saturation() * 2.0f * nutrition;
                    int tick = nutrition * 80 + (int) (saturation * 200);

                    if (fuel.hasCraftingRemainingItem()) {
                        tick += 400;
                    }

                    fuel.shrink(1);
                    fuelTick = tick;
                    maxFuelTick = tick;
                    this.setChanged();
                }
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("Energy")) {
            if (compound.contains("Energy")) {
                this.energyStorage.deserializeNBT(this.registryAccess(), compound.get("Energy"));
            }
        }
        fuelTick = compound.getInt("FuelTick");
        maxFuelTick = compound.getInt("MaxFuelTick");
        showRange = compound.getBoolean("ShowRange");
        generatorItems.clear();
        for (int i = 0; i < 2; i++) {
            generatorItems.add(ItemStack.parseOptional(this.registryAccess(), compound.getCompound("Item" + i)));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.put("Energy", this.energyStorage.serializeNBT(this.registryAccess()));
        compound.putInt("FuelTick", fuelTick);
        compound.putInt("MaxFuelTick", maxFuelTick);
        compound.putBoolean("ShowRange", showRange);
        for (int i = 0; i < generatorItems.size(); i++) {
            compound.put("Item" + i, generatorItems.get(i).save(this.registryAccess(), new CompoundTag()));
        }
    }

    public int getGeneratorContainerSize() {
        return generatorItems.size();
    }

    public boolean isGeneratorEmpty() {
        for (ItemStack stack : generatorItems) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    public ItemStack getGeneratorItem(int slot) {
        return generatorItems.get(slot);
    }

    public ItemStack removeGeneratorItem(int slot, int amount) {
        return net.minecraft.world.ContainerHelper.removeItem(generatorItems, slot, amount);
    }

    public ItemStack removeGeneratorItemNoUpdate(int slot) {
        return net.minecraft.world.ContainerHelper.takeItem(generatorItems, slot);
    }

    public void setGeneratorItem(int slot, ItemStack stack) {
        ItemStack itemstack = generatorItems.get(slot);
        boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, stack);
        generatorItems.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        if (slot == 0 && !flag) {
            this.setChanged();
        }
    }

    public void clearGeneratorContent() {
        generatorItems.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.superbwarfare.charging_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChargingStationMenu(containerId, playerInventory, new GeneratorContainerBridge(this), this.dataAccess);
    }

    /** NeoForge 1.21 通过 RegisterCapabilitiesEvent 注册能力，这里只暴露存储本身。 */
    public EnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public static class GeneratorContainerBridge implements net.minecraft.world.Container {
        private final GeneratorEntity entity;

        public GeneratorContainerBridge(GeneratorEntity entity) {
            this.entity = entity;
        }

        @Override
        public int getContainerSize() {
            return entity.getGeneratorContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return entity.isGeneratorEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return entity.getGeneratorItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return entity.removeGeneratorItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return entity.removeGeneratorItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            entity.setGeneratorItem(slot, stack);
        }

        @Override
        public boolean stillValid(Player player) {
            return player.distanceToSqr(entity) <= 64.0D;
        }

        @Override
        public void clearContent() {
            entity.clearGeneratorContent();
        }

        @Override
        public void setChanged() {
            entity.setChanged();
        }
    }
}