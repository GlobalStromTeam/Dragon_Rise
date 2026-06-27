package com.redabysslucia.dragonrise_reforge.resource.model;

import com.redabysslucia.dragonrise_reforge.client.model.entity.BedrockVehicleModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class VehicleLODModelReloadListener extends BedrockModelReloadListener<BedrockVehicleModel> {
    public static final VehicleLODModelReloadListener INSTANCE = new VehicleLODModelReloadListener();

    private VehicleLODModelReloadListener() {
        super("models/bedrock/vehicle_lod");
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModelPOJO> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        super.apply(map, resourceManager, profiler);
        for (Map.Entry<ResourceLocation, BedrockModelPOJO> entry : map.entrySet()) {
            BedrockVehicleModel model = new BedrockVehicleModel(entry.getValue());
            model.init();
            this.models.put(entry.getKey(), model);
        }
    }
}