package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.item.*;
import com.redabysslucia.dragonrise_reforge.item.armor.CN21;
import com.redabysslucia.dragonrise_reforge.item.armor.CNJustchest;
import com.redabysslucia.dragonrise_reforge.item.armor.CNfast;
import com.redabysslucia.dragonrise_reforge.item.armor.CNchest;
import com.redabysslucia.dragonrise_reforge.item.armor.MSVChest;
import com.redabysslucia.dragonrise_reforge.item.armor.Gorka3;
import com.redabysslucia.dragonrise_reforge.item.armor.AljinHelmet;
import com.redabysslucia.dragonrise_reforge.item.armor.Gorka3Leggings;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
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
    public static final RegistryObject<Item> SF_TAB_ICON = REGISTRY.register("sf_tab_icon", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SOC_TAB_ICON = REGISTRY.register("soc_tab_icon", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WW2_TAB_ICON = REGISTRY.register("ww2_tab_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MISC_ICON = REGISTRY.register("misc_icon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MK19_DEPLOYER = REGISTRY.register("mk19", MK19Deployer::new);
    public static final RegistryObject<Item> HJ8_DEPLOYER = REGISTRY.register("hj8", HJ8Deployer::new);
    public static final RegistryObject<Item> ZU23_DEPLOYER = REGISTRY.register("zu23", ZU23Deployer::new);
    public static final RegistryObject<Item> qjz89_DEPLOYER = REGISTRY.register("qjz89", qjz89Deployer::new);
    public static final RegistryObject<Item> DSHK_DEPLOYER = REGISTRY.register("dshk", DSHKDeployer::new);
    public static final RegistryObject<Item> M2_DEPLOYER = REGISTRY.register("m2", M2Deployer::new);
    public static final RegistryObject<Item> SHIELD_DEPLOYER = REGISTRY.register("shield", SHIELDDeployer::new);
    public static final RegistryObject<Item> CN21 = REGISTRY.register("cn21", CN21::new);
    public static final RegistryObject<Item> CNJUSTCHEST = REGISTRY.register("cnjustchest", CNJustchest::new);
    public static final RegistryObject<Item> CNCHEST = REGISTRY.register("cnchest", CNchest::new);
    public static final RegistryObject<Item> CNFAST = REGISTRY.register("cnfast", CNfast::new);
    public static final RegistryObject<Item> MSV_CHEST = REGISTRY.register("msv_chest", MSVChest::new);
    public static final RegistryObject<Item> GORKA3 = REGISTRY.register("gorka3", Gorka3::new);
    public static final RegistryObject<Item> ALJIN_HELMET = REGISTRY.register("aljin_helmet", AljinHelmet::new);
    public static final RegistryObject<Item> GORKA3_LEGGINGS = REGISTRY.register("gorka3_leggings", Gorka3Leggings::new);


}
