package com.redabysslucia.dragonrise_reforge.resource.model;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.Map;

public class BasicModelReloadListener extends BedrockModelReloadListener<BedrockModel> {
    public BasicModelReloadListener(String path) {
        super("models/bedrock/" + path, "animations/bedrock/" + path);
    }

    @Override
    protected void apply(Map<ResourceLocation, BedrockModelPOJO> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        super.apply(map, resourceManager, profiler);
        for (Map.Entry<ResourceLocation, BedrockModelPOJO> entry : map.entrySet()) {
            this.models.put(entry.getKey(), new BedrockModel(entry.getValue()));
        }
        for (Map.Entry<ResourceLocation, com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile> entry : this.animFiles.entrySet()) {
            ResourceLocation id = this.animPathToIds.get(entry.getKey());
            if (id == null) continue;
            ResourceLocation path = this.idToModelPaths.get(id);
            if (path == null) continue;
            BedrockModel model = this.models.get(path);
            if (model == null) continue;
            List<BedrockAnimation> animations = BedrockAnimation.createAnimation(entry.getValue(), model);
            this.animations.put(entry.getKey(), animations);
        }
        this.animFiles.clear();
    }
}