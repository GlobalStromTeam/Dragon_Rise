package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.item.MK19Deployer;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.item.ZU23Deployer;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Dragonrise_reforge.MODID);

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }

    public static final RegistryObject<Item> TAB_ICON = REGISTRY.register("tab_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MK19_DEPLOYER = REGISTRY.register("mk19", MK19Deployer::new);
    public static final RegistryObject<Item> ZU23_DEPLOYER = REGISTRY.register("zu23", ZU23Deployer::new);

}
