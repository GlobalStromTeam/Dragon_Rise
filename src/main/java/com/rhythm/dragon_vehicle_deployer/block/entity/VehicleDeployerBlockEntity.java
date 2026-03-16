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
    private int tickCounter = 0;

    /** UUID of the vehicle spawned by this deployer, null if none */
    @Nullable
    private UUID spawnedVehicleUUID = null;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> spawnIntervalSeconds;
                case 1 -> autoSpawnEnabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> spawnIntervalSeconds = value;
                case 1 -> autoSpawnEnabled = value != 0;
            }
            setChanged();
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public VehicleDeployerBlockEntity(BlockPos pos, BlockState state) {
        super(DragonVehicleDeployer.VEHICLE_DEPLOYER_BLOCK_ENTITY.get(), pos, state);
        try {
            this.spawnIntervalSeconds = Config.DEFAULT_SPAWN_INTERVAL.get();
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
        if (tag.hasUUID("SpawnedVehicleUUID")) {
            this.spawnedVehicleUUID = tag.getUUID("SpawnedVehicleUUID");
        } else {
            this.spawnedVehicleUUID = null;
        }
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
        if (this.spawnedVehicleUUID != null) {
            tag.putUUID("SpawnedVehicleUUID", this.spawnedVehicleUUID);
        }
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

    public static void tick(Level world, BlockPos pos, BlockState state, VehicleDeployerBlockEntity blockEntity) {
        if (world.isClientSide) return;
        if (!blockEntity.autoSpawnEnabled) return;
        if (!blockEntity.entityData.contains("EntityType")) return;

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
