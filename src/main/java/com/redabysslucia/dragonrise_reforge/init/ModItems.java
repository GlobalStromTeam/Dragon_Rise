package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.item.*;
import com.redabysslucia.dragonrise_reforge.item.armor.*;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(Registries.ITEM, Dragonrise_reforge.MODID);

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }

    public static final DeferredHolder<Item, Item> TAB_ICON = REGISTRY.register("tab_icon", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SF_TAB_ICON = REGISTRY.register("sf_tab_icon", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOC_TAB_ICON = REGISTRY.register("soc_tab_icon", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> WW2_TAB_ICON = REGISTRY.register("ww2_tab_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MISC_ICON = REGISTRY.register("misc_icon", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MK19_DEPLOYER = REGISTRY.register("mk19", MK19Deployer::new);
    public static final DeferredHolder<Item, Item> HJ8_DEPLOYER = REGISTRY.register("hj8", HJ8Deployer::new);
    public static final DeferredHolder<Item, Item> R9M133_DEPLOYER = REGISTRY.register("9m133", R9M133Deployer::new);
    public static final DeferredHolder<Item, Item> ZU23_DEPLOYER = REGISTRY.register("zu23", ZU23Deployer::new);
    public static final DeferredHolder<Item, Item> qjz89_DEPLOYER = REGISTRY.register("qjz89", qjz89Deployer::new);
    public static final DeferredHolder<Item, Item> DSHK_DEPLOYER = REGISTRY.register("dshk", DSHKDeployer::new);
    public static final DeferredHolder<Item, Item> M2_DEPLOYER = REGISTRY.register("m2", M2Deployer::new);
    public static final DeferredHolder<Item, Item> SHIELD_DEPLOYER = REGISTRY.register("shield", SHIELDDeployer::new);
    public static final DeferredHolder<Item, Item> AMMO_SUPPLY_STATION_DEPLOYER = REGISTRY.register("ammo_supply_station", AmmoSupplyStationDeployer::new);
    public static final DeferredHolder<Item, Item> GENERATOR_DEPLOYER = REGISTRY.register("generator_deployer", GeneratorDeployer::new);
    public static final DeferredHolder<Item, Item> CN21 = REGISTRY.register("cn21", CN21::new);
    public static final DeferredHolder<Item, Item> CNCHEST = REGISTRY.register("cnchest", CNchest::new);
    public static final DeferredHolder<Item, Item> CNFAST = REGISTRY.register("cnfast", CNfast::new);
    public static final DeferredHolder<Item, Item> MSV_CHEST = REGISTRY.register("msv_chest", MSVChest::new);
    public static final DeferredHolder<Item, Item> GORKA3 = REGISTRY.register("gorka3", Gorka3::new);
    public static final DeferredHolder<Item, Item> ALJIN_HELMET = REGISTRY.register("aljin_helmet", AljinHelmet::new);
    public static final DeferredHolder<Item, Item> GORKA3_LEGGINGS = REGISTRY.register("gorka3_leggings", Gorka3Leggings::new);
    public static final DeferredHolder<Item, Item> MED21_CHEST = REGISTRY.register("med21_chest", MED21Chest::new);
    public static final DeferredHolder<Item, Item> T21_HELMET = REGISTRY.register("t21_helmet", T21Helmet::new);
    public static final DeferredHolder<Item, Item> FAST_HELMET = REGISTRY.register("fast_helmet", FASTHelmet::new);
    public static final DeferredHolder<Item, Item> SNIPER21_HELMET = REGISTRY.register("sniper21_helmet", Sniper21Helmet::new);
    public static final DeferredHolder<Item, Item> PANTS21 = REGISTRY.register("pants21", Pants21::new);
    public static final DeferredHolder<Item, Item> MSV_PANTS = REGISTRY.register("msv_pants", MSVPants::new);
    public static final DeferredHolder<Item, Item> DESERT07_HELMET = REGISTRY.register("desert07_helmet", Desert07Helmet::new);
    public static final DeferredHolder<Item, Item> DESERT07_CHEST = REGISTRY.register("desert07_chest", Desert07Chest::new);
    public static final DeferredHolder<Item, Item> DESERT07_PANTS = REGISTRY.register("desert07_pants", Desert07Pants::new);
    public static final DeferredHolder<Item, Item> OCEAN07_HELMET = REGISTRY.register("ocean07_helmet", Ocean07Helmet::new);
    public static final DeferredHolder<Item, Item> OCEAN07_CHEST = REGISTRY.register("ocean07_chest", Ocean07Chest::new);
    public static final DeferredHolder<Item, Item> OCEAN07_PANTS = REGISTRY.register("ocean07_pants", Ocean07Pants::new);
    public static final DeferredHolder<Item, Item> KR06_HELMET = REGISTRY.register("kr06_helmet", KR06Helmet::new);
    public static final DeferredHolder<Item, Item> KR06_CHEST = REGISTRY.register("kr06_chest", KR06Chest::new);
    public static final DeferredHolder<Item, Item> KR06_PANTS = REGISTRY.register("kr06_pants", KR06Pants::new);

    public static final DeferredHolder<Item, Item> TERRORIST_SPAWN_EGG = REGISTRY.register("terrorist_spawn_egg",
        () -> new SpawnEggItem(ModEntities.TERRORIST.get(), -11584987, -14014413, new Item.Properties()));
    public static final DeferredHolder<Item, Item> UN_HELMET = REGISTRY.register("un_helmet", UNHelmet::new);

    public static final DeferredHolder<Item, Item> SPRAY_CAN = REGISTRY.register("spray_can", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CLUSTER_CHARGE = REGISTRY.register("cluster_charge", () -> new ClusterChargeItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> R6_DRONE = REGISTRY.register("r6_drone", () -> new R6DroneDeployerItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ATTACK_DRONE = REGISTRY.register("attack_drone", () -> new AttackDroneDeployerItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KEVLAR = REGISTRY.register("kevlar", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KV1_DEPLOYER = REGISTRY.register("kv1", KV1Deployer::new);
    public static final DeferredHolder<Item, Item> PAK40_DEPLOYER = REGISTRY.register("pak40", Pak40Deployer::new);

}
