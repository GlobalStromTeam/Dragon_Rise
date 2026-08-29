package com.rhythm.dragon_vehicle_deployer;


import net.neoforged.fml.common.EventBusSubscriber;import com.rhythm.dragon_vehicle_deployer.block.VehicleDeployerBlock;
import com.rhythm.dragon_vehicle_deployer.block.entity.VehicleDeployerBlockEntity;
import com.rhythm.dragon_vehicle_deployer.client.screen.DeployerConfigScreen;
import com.rhythm.dragon_vehicle_deployer.menu.ModMenuTypes;
import com.rhythm.dragon_vehicle_deployer.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.client.overlay.HJ8Overlay;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

public class DragonVehicleDeployer {
    public static final String MODID = "dragonrise_reforge";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<Block, Block> VEHICLE_DEPLOYER_BLOCK = BLOCKS.register("vehicle_deployer", () -> new VehicleDeployerBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(3.0F, 3600000.0F)));
    public static final DeferredHolder<Item, Item> VEHICLE_DEPLOYER_BLOCK_ITEM = ITEMS.register("vehicle_deployer", () -> new BlockItem(VEHICLE_DEPLOYER_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VehicleDeployerBlockEntity>> VEHICLE_DEPLOYER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("vehicle_deployer", () -> BlockEntityType.Builder.of(VehicleDeployerBlockEntity::new, VEHICLE_DEPLOYER_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(DragonVehicleDeployer::commonSetup);
        modEventBus.addListener(ModNetwork::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.REGISTRY.register(modEventBus);

        NeoForge.EVENT_BUS.register(DragonVehicleDeployer.class);

        modEventBus.addListener(DragonVehicleDeployer::addCreative);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("DRAGON VEHICLE DEPLOYER: COMMON SETUP");
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(new ItemStack(VEHICLE_DEPLOYER_BLOCK_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("DRAGON VEHICLE DEPLOYER: Server starting");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.DEPLOYER_CONFIG_MENU.get(), DeployerConfigScreen::new);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiLayersEvent event) {
            event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(MODID, "hj8"), new HJ8Overlay());
        }
    }
}
