package com.redabysslucia.dragonrise_reforge.datagen;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Dragonrise_reforge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new ModWreckageLootProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new GeoOBBDataProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new VehicleJavaGenerator(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new VehicleLanguageGenerator(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new MeshModelNormalRemover(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new VehicleSkinGenerator(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new VehicleDataCompletenessChecker(packOutput, existingFileHelper));
    }
}
