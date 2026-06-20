package com.rhythm.dragon_vehicle_deployer.menu;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(Registries.MENU, DragonVehicleDeployer.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DeployerConfigMenu>> DEPLOYER_CONFIG_MENU =
            REGISTRY.register("deployer_config_menu",
                    () -> new MenuType<>((id, inv) -> new DeployerConfigMenu(id, inv, BlockPos.ZERO), FeatureFlags.VANILLA_SET) {
                        @Override
                        public DeployerConfigMenu create(int windowId, Inventory inv, RegistryFriendlyByteBuf buf) {
                            return new DeployerConfigMenu(windowId, inv, buf.readBlockPos());
                        }
                    });
}
