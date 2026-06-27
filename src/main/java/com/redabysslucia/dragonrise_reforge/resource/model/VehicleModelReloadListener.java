package com.redabysslucia.dragonrise_reforge.resource.model;

import com.redabysslucia.dragonrise_reforge.client.model.entity.BedrockVehicleModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.Map;

public class VehicleModelReloadListener extends BedrockModelReloadListener<BedrockVehicleModel> {
    public static final VehicleModelReloadListener INSTANCE = new VehicleModelReloadListener();

    private VehicleModelReloadListener() {
        super("models/bedrock/vehicle", "animations/bedrock/vehicle");
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModelPOJO> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        super.apply(map, resourceManager, profiler);
        for (Map.Entry<ResourceLocation, BedrockModelPOJO> entry : map.entrySet()) {
            BedrockVehicleModel model = new BedrockVehicleModel(entry.getValue());
            model.init();
            this.models.put(entry.getKey(), model);
        }

        for (Map.Entry<ResourceLocation, com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile> entry : this.animFiles.entrySet()) {
            ResourceLocation id = this.animPathToIds.get(entry.getKey());
            if (id == null) continue;
            ResourceLocation path = this.idToModelPaths.get(id);
            if (path == null) continue;
            BedrockVehicleModel model = this.models.get(path);
            if (model == null) continue;
            List<BedrockAnimation> animations = BedrockAnimation.createAnimation(entry.getValue(), model);
            this.animations.put(entry.getKey(), animations);
        }
        this.animFiles.clear();
    }
}