package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.client.renderer.entity.*;
import com.redabysslucia.dragonrise_reforge.entities.AA625EEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
                event.registerEntityRenderer(ModEntities.ZTZ99A.get(), ZTZ99ARenderer::new);
                event.registerEntityRenderer(ModEntities.KA50.get(), KA50Renderer::new);
                event.registerEntityRenderer(ModEntities.T80.get(), T80Renderer::new);
                event.registerEntityRenderer(ModEntities.J10.get(), J10Renderer::new);
                event.registerEntityRenderer(ModEntities.AA625E.get(), AA625ERenderer::new);
        }

}
