package com.redabysslucia.dragonrise_reforge.client.model.entity.projectile;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AAshellEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AAshellModel extends GeoModel<AAshellEntity> {

    @Override
    public ResourceLocation getAnimationResource(AAshellEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "animations/airbomb500kg.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(AAshellEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "geo/small_cannon_shell.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AAshellEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/airbomb500kg.png");
    }
}
