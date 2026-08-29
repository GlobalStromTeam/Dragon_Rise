package com.rhythm.dragon_vehicle_deployer.menu;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, DragonVehicleDeployer.MODID);

    public static final RegistryObject<MenuType<DeployerConfigMenu>> DEPLOYER_CONFIG_MENU =
            REGISTRY.register("deployer_config_menu",
                    () -> MenuType.create((windowId, inv, data) -> {
                        var pos = data.readBlockPos();
                        return new DeployerConfigMenu(windowId, inv, pos);
                    }));
}
