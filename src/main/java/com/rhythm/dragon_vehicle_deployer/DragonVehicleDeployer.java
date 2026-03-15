package com.rhythm.dragon_vehicle_deployer;

import com.rhythm.dragon_vehicle_deployer.block.VehicleDeployerBlock;
import com.rhythm.dragon_vehicle_deployer.block.entity.VehicleDeployerBlockEntity;
import com.rhythm.dragon_vehicle_deployer.client.screen.DeployerConfigScreen;
import com.rhythm.dragon_vehicle_deployer.menu.ModMenuTypes;
import com.rhythm.dragon_vehicle_deployer.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

public class DragonVehicleDeployer {
    public static final String MODID = "dragonrise_reforge";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<Block> VEHICLE_DEPLOYER_BLOCK = BLOCKS.register("vehicle_deployer", VehicleDeployerBlock::new);
    public static final RegistryObject<Item> VEHICLE_DEPLOYER_BLOCK_ITEM = ITEMS.register("vehicle_deployer", () -> new BlockItem(VEHICLE_DEPLOYER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<VehicleDeployerBlockEntity>> VEHICLE_DEPLOYER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vehicle_deployer", () -> BlockEntityType.Builder.of(VehicleDeployerBlockEntity::new, VEHICLE_DEPLOYER_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DragonVehicleDeployer::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.REGISTRY.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(DragonVehicleDeployer.class);

        modEventBus.addListener(DragonVehicleDeployer::addCreative);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("DRAGON VEHICLE DEPLOYER: COMMON SETUP");
        ModNetwork.register(event);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(VEHICLE_DEPLOYER_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("DRAGON VEHICLE DEPLOYER: Server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("DRAGON VEHICLE DEPLOYER: CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            event.enqueueWork(() ->
                    MenuScreens.register(ModMenuTypes.DEPLOYER_CONFIG_MENU.get(), DeployerConfigScreen::new)
            );
        }
    }
}
