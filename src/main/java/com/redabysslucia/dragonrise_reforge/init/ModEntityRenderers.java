package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.client.renderer.entity.J10Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.KA50Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.T80Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.ZTZ99aRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
                event.registerEntityRenderer(ModEntities.ZTZ99A.get(), ZTZ99aRenderer::new);
                event.registerEntityRenderer(ModEntities.KA50.get(), KA50Renderer::new);
                event.registerEntityRenderer(ModEntities.T80.get(), T80Renderer::new);
                event.registerEntityRenderer(ModEntities.J10.get(), J10Renderer::new);
        }

}
