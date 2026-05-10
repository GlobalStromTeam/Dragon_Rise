package com.redabysslucia.dragonrise_reforge.datagen.base;

import com.atsuishio.superbwarfare.data.loot.WreckageLootData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SbwWreckageLootProvider implements DataProvider {
    protected final PackOutput output;
    protected final ExistingFileHelper existingFileHelper;
    protected final List<WreckageLootData> lootData = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public SbwWreckageLootProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    public abstract void generate();

    public void add(EntityType<? extends VehicleEntity> type, WreckageLootData.Builder builder) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id != null) {
            lootData.add(builder.build(id));
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        this.generate();

        List<CompletableFuture<?>> list = new ArrayList<>();
        var pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "sbw/loot");

        for (var data : lootData) {
            var id = data.getId();
            if (existingFileHelper.exists(id, PackType.SERVER_DATA, ".json", "sbw/loot")) {
                throw new IllegalArgumentException("Duplicate wreckage loot data: " + id);
            }
            var path = pathProvider.json(id);
            list.add(DataProvider.saveStable(pOutput, GSON.toJsonTree(data), path));
        }

        return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "DragonRise Wreckage Loot";
    }
}
