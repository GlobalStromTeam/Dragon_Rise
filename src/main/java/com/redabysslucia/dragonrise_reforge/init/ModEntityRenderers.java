package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.client.renderer.entity.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
                event.registerEntityRenderer(ModEntities.ZTZ99A.get(), ZTZ99ARenderer::new);
                event.registerEntityRenderer(ModEntities.ZTZ99BH.get(), ZTZ99BHRenderer::new);
                event.registerEntityRenderer(ModEntities.M1A2SEPV2.get(), M1A2SEPV2Renderer::new);
                event.registerEntityRenderer(ModEntities.AMX56.get(), AMX56Renderer::new);
                event.registerEntityRenderer(ModEntities.CV90.get(), CV90Renderer::new);
                event.registerEntityRenderer(ModEntities.KA50.get(), KA50Renderer::new);
                event.registerEntityRenderer(ModEntities.T80.get(), T80Renderer::new);
                event.registerEntityRenderer(ModEntities.AA625E.get(), AA625ERenderer::new);
                event.registerEntityRenderer(ModEntities.J10.get(), J10Renderer::new);
                event.registerEntityRenderer(ModEntities.REFALE.get(), REFALERenderer::new);
                event.registerEntityRenderer(ModEntities.Z10ME.get(), Z10MERenderer::new);
                event.registerEntityRenderer(ModEntities.ZBL08.get(), ZBL08Renderer::new);
                event.registerEntityRenderer(ModEntities.ZLT_11.get(), ZLT11Renderer::new);
                event.registerEntityRenderer(ModEntities.ZBD04A.get(), ZBD04ARenderer::new);
                event.registerEntityRenderer(ModEntities.SX1.get(), SX1Renderer::new);
                event.registerEntityRenderer(ModEntities.STRV103.get(), STRV103Renderer::new);
                event.registerEntityRenderer(ModEntities.F14.get(), F14Renderer::new);
        }

}
