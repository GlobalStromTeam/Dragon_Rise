package com.redabysslucia.dragonrise_reforge.client.model.entity.projectile;

import com.atsuishio.superbwarfare.Mod;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AAshellEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class AAshellModel extends GeoModel<AAshellEntity> {

    @Override
    public ResourceLocation getAnimationResource(AAshellEntity entity) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(AAshellEntity entity) {
        return Mod.loc("geo/small_cannon_shell.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AAshellEntity entity) {
        return Mod.loc("textures/entity/small_cannon_shell.png");
    }

    @Override
    public void setCustomAnimations(AAshellEntity animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone bone = getAnimationProcessor().getBone("bone");
        bone.setScaleY((float) (1 + 2 * animatable.getDeltaMovement().length()));
    }
}