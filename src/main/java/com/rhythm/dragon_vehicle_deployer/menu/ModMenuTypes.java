package com.rhythm.dragon_vehicle_deployer.menu;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, DragonVehicleDeployer.MODID);

    public static final RegistryObject<MenuType<DeployerConfigMenu>> DEPLOYER_CONFIG_MENU =
            REGISTRY.register("deployer_config_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        var pos = data.readBlockPos();
                        return new DeployerConfigMenu(windowId, inv, pos);
                    }));
}
