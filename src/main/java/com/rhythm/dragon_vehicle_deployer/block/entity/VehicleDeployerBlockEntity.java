package com.rhythm.dragon_vehicle_deployer.block.entity;

import com.rhythm.dragon_vehicle_deployer.Config;
import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import com.rhythm.dragon_vehicle_deployer.block.VehicleDeployerBlock;
import com.rhythm.dragon_vehicle_deployer.menu.DeployerConfigMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Math;

import javax.annotation.Nullable;
import java.util.UUID;

public class VehicleDeployerBlockEntity extends BlockEntity implements MenuProvider {
    public CompoundTag entityData = new CompoundTag();

    public int spawnIntervalSeconds = 5;
    public boolean autoSpawnEnabled = true;
    public int idleClearTimeoutSeconds = 300;
    private int tickCounter = 0;

    /** UUID of the vehicle spawned by this deployer, null if none */
    @Nullable
    private UUID spawnedVehicleUUID = null;

    /** Whether the spawned vehicle has ever been occupied by a player */
    private boolean vehicleWasOccupied = false;

    /** Game time when the vehicle last had a passenger */
    private long lastOccupiedGameTime = 0;

    /** Flag set by trackOccupancy() to trigger immediate redeploy after idle clear */
    private boolean idleClearTriggered = false;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> spawnIntervalSeconds;
                case 1 -> autoSpawnEnabled ? 1 : 0;
                case 2 -> idleClearTimeoutSeconds;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> spawnIntervalSeconds = value;
                case 1 -> autoSpawnEnabled = value != 0;
                case 2 -> idleClearTimeoutSeconds = value;
            }
            setChanged();
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public VehicleDeployerBlockEntity(BlockPos pos, BlockState state) {
        super(DragonVehicleDeployer.VEHICLE_DEPLOYER_BLOCK_ENTITY.get(), pos, state);
        try {
            this.spawnIntervalSeconds = Config.DEFAULT_SPAWN_INTERVAL.get();
            this.idleClearTimeoutSeconds = Config.IDLE_CLEAR_TIMEOUT_SECONDS.get();
        } catch (IllegalStateException ignored) {
            // Config not yet loaded during registration
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.entityData = new CompoundTag();
        if (tag.contains("EntityType")) {
            this.entityData.putString("EntityType", tag.getString("EntityType"));
        }
        if (tag.contains("Entity")) {
            this.entityData.put("Entity", tag.getCompound("Entity"));
        }
        this.spawnIntervalSeconds = tag.contains("SpawnIntervalSeconds") ? tag.getInt("SpawnIntervalSeconds") : Config.DEFAULT_SPAWN_INTERVAL.get();
        this.autoSpawnEnabled = tag.contains("AutoSpawnEnabled") ? tag.getBoolean("AutoSpawnEnabled") : true;
        this.idleClearTimeoutSeconds = tag.contains("IdleClearTimeoutSeconds") ? tag.getInt("IdleClearTimeoutSeconds") : Config.IDLE_CLEAR_TIMEOUT_SECONDS.get();
        if (tag.hasUUID("SpawnedVehicleUUID")) {
            this.spawnedVehicleUUID = tag.getUUID("SpawnedVehicleUUID");
        } else {
            this.spawnedVehicleUUID = null;
        }
        this.vehicleWasOccupied = tag.getBoolean("VehicleWasOccupied");
        this.lastOccupiedGameTime = tag.getLong("LastOccupiedGameTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.entityData.contains("EntityType")) {
            tag.putString("EntityType", this.entityData.getString("EntityType"));
        }
        if (this.entityData.contains("Entity")) {
            tag.put("Entity", this.entityData.getCompound("Entity"));
        }
        tag.putInt("SpawnIntervalSeconds", this.spawnIntervalSeconds);
        tag.putBoolean("AutoSpawnEnabled", this.autoSpawnEnabled);
        tag.putInt("IdleClearTimeoutSeconds", this.idleClearTimeoutSeconds);
        if (this.spawnedVehicleUUID != null) {
            tag.putUUID("SpawnedVehicleUUID", this.spawnedVehicleUUID);
        }
        tag.putBoolean("VehicleWasOccupied", this.vehicleWasOccupied);
        tag.putLong("LastOccupiedGameTime", this.lastOccupiedGameTime);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    public void writeEntityInfo(ItemStack stack) {
        var tag = BlockItem.getBlockEntityData(stack);
        if (tag == null) return;
        this.entityData = tag.copy();
        this.spawnedVehicleUUID = null;
        this.vehicleWasOccupied = false;
        this.lastOccupiedGameTime = 0;
        this.idleClearTriggered = false;
        this.setChanged();
    }

    public void deploy(BlockState state) {
        if (this.level == null) return;

        if (this.entityData.contains("EntityType")) {
            var entityType = EntityType.byString(entityData.getString("EntityType")).orElse(null);
            if (entityType == null) return;

            var entity = entityType.create(this.level);
            if (entity == null) return;

            if (entityData.contains("Entity")) {
                var entityTag = entityData.getCompound("Entity").copy();
                entityTag.remove("UUID");
                entity.load(entityTag);
            }

            var direction = state.getValue(VehicleDeployerBlock.FACING);

            UUID newUUID = UUID.randomUUID();
            entity.setUUID(newUUID);
            entity.setPos(this.getBlockPos().getX() + 0.5 + (2 * Math.random() - 1) * 0.1f, this.getBlockPos().getY() + 1.5 + (2 * Math.random() - 1) * 0.1f, this.getBlockPos().getZ() + 0.5 + (2 * Math.random() - 1) * 0.1f);
            entity.setYRot(direction.toYRot());
            this.level.addFreshEntity(entity);

            this.spawnedVehicleUUID = newUUID;
            this.setChanged();
        }
    }

    private boolean isSpawnedVehicleAlive() {
        if (!(this.level instanceof ServerLevel serverLevel)) return false;
        if (this.spawnedVehicleUUID == null) return false;

        Entity entity = serverLevel.getEntity(this.spawnedVehicleUUID);
        return entity != null && entity.isAlive();
    }

    /**
     * Tracks occupancy of the spawned vehicle and clears it if idle too long.
     * Called every tick from {@link #tick}.
     */
    private void trackOccupancy(Level world) {
        int timeout = this.idleClearTimeoutSeconds;
        if (timeout <= 0) return;
        if (!(world instanceof ServerLevel serverLevel)) return;
        if (this.spawnedVehicleUUID == null) {
            this.vehicleWasOccupied = false;
            this.lastOccupiedGameTime = 0;
            return;
        }

        Entity entity = serverLevel.getEntity(this.spawnedVehicleUUID);
        if (entity == null || !entity.isAlive()) {
            this.vehicleWasOccupied = false;
            this.lastOccupiedGameTime = 0;
            this.spawnedVehicleUUID = null;
            return;
        }

        boolean hasPassengers = !entity.getPassengers().isEmpty();

        if (hasPassengers) {
            this.vehicleWasOccupied = true;
            this.lastOccupiedGameTime = world.getGameTime();
        } else if (this.vehicleWasOccupied) {
            long idleTicks = world.getGameTime() - this.lastOccupiedGameTime;
            if (idleTicks >= timeout * 20L) {
                entity.discard();
                this.spawnedVehicleUUID = null;
                this.vehicleWasOccupied = false;
                this.lastOccupiedGameTime = 0;
                this.idleClearTriggered = true;
                this.tickCounter = 0;
                this.setChanged();
            }
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState state, VehicleDeployerBlockEntity blockEntity) {
        if (world.isClientSide) return;
        if (!blockEntity.autoSpawnEnabled) return;
        if (!blockEntity.entityData.contains("EntityType")) return;

        // Track occupancy and check for idle timeout
        blockEntity.trackOccupancy(world);

        // If vehicle was just cleared due to idle timeout, deploy immediately
        if (blockEntity.idleClearTriggered) {
            blockEntity.idleClearTriggered = false;
            blockEntity.deploy(state);
            return;
        }

        blockEntity.tickCounter++;
        if (blockEntity.tickCounter < blockEntity.spawnIntervalSeconds * 20) return;

        blockEntity.tickCounter = 0;

        if (blockEntity.isSpawnedVehicleAlive()) return;

        blockEntity.deploy(state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.dragonrise_reforge.deployer_config");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DeployerConfigMenu(containerId, playerInventory, this.getBlockPos(), this.dataAccess);
    }
}
