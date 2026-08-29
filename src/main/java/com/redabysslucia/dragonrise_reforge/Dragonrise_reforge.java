package com.redabysslucia.dragonrise_reforge;

import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.client.outline.render.OutlineRenderer;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import com.redabysslucia.dragonrise_reforge.init.ModTabs;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

@SuppressWarnings("removal")

@Mod(Dragonrise_reforge.MODID)
public class Dragonrise_reforge {

        public static final String MODID = "dragonrise_reforge";

        public static final Logger LOGGER = LogUtils.getLogger();

        public Dragonrise_reforge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(bus);
        ModEntities.REGISTRY.register(bus);
        ModTabs.TABS.register(bus);
        ModSounds.REGISTRY.register(bus);
        DragonVehicleDeployer.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::setupClient);

        NeoForge.EVENT_BUS.register(this);
    }

        private void commonSetup(final FMLCommonSetupEvent event) {
                // Some common setup code
                LOGGER.info("HELLO FROM COMMON SETUP");
                LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
                ModNetwork.register();
        }

        //夜的视

//        public static final Lazy<KeyMapping> SWITCH_MODE_KEY = Lazy.of(() -> new KeyMapping(
//                "DR Mode", KeyConflictContext.IN_GAME,
//                InputConstants.getKey("key.keyboard.x"), "Realistic Night Vision"
//        ));


        public void dragonrise_reforge() {
                NeoForge.EVENT_BUS.register(this);
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        }

        private void setupClient(final FMLClientSetupEvent event) {
                event.enqueueWork(() -> {
                        // Initialize outline rendering system
                        try {
                            OutlineRenderer.init();
                            OutlineRenderer.register(); // 注册到事件总线
                            LOGGER.info("Outline rendering system initialized");
                        } catch (Exception e) {
                            LOGGER.error("Failed to initialize outline rendering system", e);
                        }
                });
        }

//        @SubscribeEvent
//        public void registerKeyMappings(RegisterKeyMappingsEvent event) {
//                event.register(SWITCH_MODE_KEY.get());
//        }

}