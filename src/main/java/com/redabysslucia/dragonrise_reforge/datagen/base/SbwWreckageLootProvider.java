package com.redabysslucia.dragonrise_reforge.datagen.base;

import com.atsuishio.superbwarfare.data.loot.WreckageLootData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SbwWreckageLootProvider implements DataProvider {
    protected final PackOutput output;
    protected final ExistingFileHelper existingFileHelper;
    protected final List<WreckageLootData> lootData = new ArrayList<>();

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

    /**
     * 序列化为 SBW 官方格式（kotlinx.serialization）：
     * 字段 PascalCase（ID/Pools/Entries/Count/Name/Chance/Source/Type），
     * Type 枚举小写下划线（complete/default/turret_only/vehicle_only）。
     * 不能用 Gson 直接反射 Kotlin 类，否则输出小写字段导致运行期解析失败。
     */
    private JsonObject serialize(WreckageLootData data) {
        JsonObject root = new JsonObject();
        root.addProperty("ID", data.getId().toString());

        JsonArray pools = new JsonArray();
        for (WreckageLootData.Pool pool : data.getPools()) {
            JsonObject poolObj = new JsonObject();

            JsonArray entries = new JsonArray();
            for (WreckageLootData.Entry entry : pool.getEntries()) {
                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("Name", entry.getName());
                entryObj.addProperty("Count", entry.getCount());
                entryObj.addProperty("Chance", entry.getChance());
                entries.add(entryObj);
            }
            poolObj.add("Entries", entries);

            poolObj.addProperty("Rolls", pool.getRolls());
            poolObj.addProperty("Source", pool.getSource());
            // TURRET_ONLY -> turret_only, VEHICLE_ONLY -> vehicle_only, COMPLETE -> complete, DEFAULT -> default
            poolObj.addProperty("Type", pool.getType().name().toLowerCase());

            pools.add(poolObj);
        }
        root.add("Pools", pools);

        return root;
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
            list.add(DataProvider.saveStable(pOutput, serialize(data), path));
        }

        return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "DragonRise Wreckage Loot";
    }
}
