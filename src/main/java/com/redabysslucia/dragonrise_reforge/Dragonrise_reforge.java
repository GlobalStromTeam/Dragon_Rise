package com.redabysslucia.dragonrise_reforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import com.redabysslucia.dragonrise_reforge.init.ModItems;
import com.redabysslucia.dragonrise_reforge.init.ModTabs;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

        private static final Logger LOGGER = LogUtils.getLogger();

        public Dragonrise_reforge() {
                IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

                ModItems.register(bus);
                ModEntities.REGISTRY.register(bus);
                ModTabs.TABS.register(bus);

                bus.addListener(this::commonSetup);

                MinecraftForge.EVENT_BUS.register(this);
        }

        private void commonSetup(final FMLCommonSetupEvent event) {
                // Some common setup code
                LOGGER.info("HELLO FROM COMMON SETUP");
                LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        }

        //夜的视

        public static final Lazy<KeyMapping> SWITCH_MODE_KEY = Lazy.of(() -> new KeyMapping(
                "DR Mode", KeyConflictContext.IN_GAME,
                InputConstants.getKey("key.keyboard.x"), "Realistic Night Vision"
        ));
        private static boolean isGreen = true;
        private static boolean shouldHaveEffect = false;

        public void dragonrise_reforge() {
                MinecraftForge.EVENT_BUS.register(this);
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        }

        private void setupClient(final FMLClientSetupEvent event) {
                event.enqueueWork(() -> {
                });
        }

        @SubscribeEvent
        public void registerKeyMappings(RegisterKeyMappingsEvent event) {
                event.register(SWITCH_MODE_KEY.get());
        }

        public static void toggle(Boolean on) {
                if (on) {
                        String path = isGreen ? "shaders/post/night-vision-wp.json" : "shaders/post/entity_outline.json";
                        Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation(path));
                } else {
                        if (on) {
                                String path = "shaders/post/entity_outline.json";
                                Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation(path));
                        Minecraft.getInstance().gameRenderer.shutdownEffect();
                    }
                }
        }

        public static boolean hasEffectNow() {
                PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
                if (effect == null) {
                        return false;
                }
                return effect.getName().startsWith(MODID);
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
                if (event.phase == TickEvent.Phase.START) {
                        if (Minecraft.getInstance().player != null) {
                                shouldHaveEffect = Minecraft.getInstance().player.hasEffect(MobEffects.NIGHT_VISION);
                        }
                }
                if (event.phase == TickEvent.Phase.END) {
                        boolean now = hasEffectNow();
                        if (shouldHaveEffect && !now) {
                                toggle(true);
                        }
                        if (!shouldHaveEffect && now) {
                                toggle(false);
                        }
                        while (SWITCH_MODE_KEY.get().consumeClick()) {
                                if (shouldHaveEffect) {
                                        isGreen = !isGreen;
                                        toggle(true);
                                        if (Minecraft.getInstance().player != null) {
                                                Minecraft.getInstance().player.playSound(
                                                        SoundEvent.createVariableRangeEvent(
                                                                ResourceLocation.fromNamespaceAndPath(MODID, "switch_mode")
                                                        ),
                                                        1f,
                                                        1f
                                                );
                                        }
                                }
                        }
                }
        }
}
