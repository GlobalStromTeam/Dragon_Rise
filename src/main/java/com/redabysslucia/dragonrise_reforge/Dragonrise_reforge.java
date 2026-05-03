package com.redabysslucia.dragonrise_reforge;

import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.client.outline.OutlineThermal;
import com.redabysslucia.dragonrise_reforge.client.outline.render.OutlineRenderer;
import com.redabysslucia.dragonrise_reforge.client.overlay.VehicleBackgroundOverlay;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import com.redabysslucia.dragonrise_reforge.init.ModSpeedSounds;
import com.redabysslucia.dragonrise_reforge.init.ModTabs;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
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
        ModSpeedSounds.SOUNDS.register(bus);
        DragonVehicleDeployer.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::setupClient);

        MinecraftForge.EVENT_BUS.register(this);
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
                MinecraftForge.EVENT_BUS.register(this);
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        }

        private void setupClient(final FMLClientSetupEvent event) {
                event.enqueueWork(() -> {
                        // Initialize outline rendering system
                        try {
                            OutlineRenderer.init();
                            OutlineRenderer.register(); // 注册到事件总线
                            OutlineThermal.setupOutlines();
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