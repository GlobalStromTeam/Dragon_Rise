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
                event.registerEntityRenderer(ModEntities.ZTZ99A.get(), ZTZ99aRenderer::new);
                event.registerEntityRenderer(ModEntities.ZTZ99BH.get(), ZTZ99BHRenderer::new);
                event.registerEntityRenderer(ModEntities.KA50.get(), KA50Renderer::new);
                event.registerEntityRenderer(ModEntities.T80.get(), T80Renderer::new);
                event.registerEntityRenderer(ModEntities.M1A2SEPV2.get(), M1A2SEPV2Renderer::new);
        }

}
