package com.redabyss.ralmod.RalModRegist;

import com.redabyss.ralmod.RalMod;
import com.redabyss.ralmod.RalModClient.Renderer.gl6BER;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import com.redabyss.ralmod.RalModClient.Model.gl6Model;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RalMod.MODID,bus= Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientSetUp {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(BlockEntities.GL6_BE.get(), gl6BER::new);
    }
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                new ModelLayerLocation(new ResourceLocation(RalMod.MODID, "models/block/gl6"), "main"),
                gl6Model::createBodyLayer
        );
    }
}
