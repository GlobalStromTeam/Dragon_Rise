package com.redabysslucia.dragonrise_reforge;

import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.client.outline.render.OutlineRenderer;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import com.redabysslucia.dragonrise_reforge.init.ModTabs;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(Dragonrise_reforge.MODID)
public class Dragonrise_reforge {

        public static final String MODID = "dragonrise_reforge";

        public static final Logger LOGGER = LogUtils.getLogger();

        public Dragonrise_reforge(IEventBus bus, ModContainer modContainer) {

        ModItems.register(bus);
        ModEntities.REGISTRY.register(bus);
        ModTabs.TABS.register(bus);
        ModSounds.REGISTRY.register(bus);
        DragonVehicleDeployer.register(bus, modContainer);

        bus.addListener(this::commonSetup);
        bus.addListener(this::setupClient);
        bus.addListener(ModNetwork::register);
    }

        private void commonSetup(final FMLCommonSetupEvent event) {
                LOGGER.info("HELLO FROM COMMON SETUP");
                LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
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

}
