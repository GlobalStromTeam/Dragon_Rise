package com.rhythm.dragon_vehicle_deployer.menu;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class DeployerConfigMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final ContainerData data;

    // 客户端构造函数
    public DeployerConfigMenu(int windowId, Inventory inv, BlockPos pos) {
        this(windowId, inv, pos, new SimpleContainerData(2));
    }

    // 服务端构造函数
    public DeployerConfigMenu(int windowId, Inventory inv, BlockPos pos, ContainerData data) {
        super(ModMenuTypes.DEPLOYER_CONFIG_MENU.get(), windowId);
        this.pos = pos;
        this.data = data;
        this.addDataSlots(data);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getSpawnIntervalSeconds() {
        return this.data.get(0);
    }

    public boolean isAutoSpawnEnabled() {
        return this.data.get(1) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0
                && player.level().getBlockState(pos).is(DragonVehicleDeployer.VEHICLE_DEPLOYER_BLOCK.get());
    }
}
