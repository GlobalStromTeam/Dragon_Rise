package com.redabysslucia.dragonrise_reforge.client.model.ammo;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.projectile.MissileShellEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 红箭8 发射后弹出的空发射筒模型（素材：桌面 missileshell.geo.json / missleshell.png） */
public class MissileShellModel extends GeoModel<MissileShellEntity> {

    public MissileShellModel() {
    }

    @Override
    public ResourceLocation getAnimationResource(MissileShellEntity entity) {
        return null;
    }

    @Override
    public ResourceLocation getModelResource(MissileShellEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "geo/missileshell.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MissileShellEntity entity) {
        return new ResourceLocation(Dragonrise_reforge.MODID, "textures/entity/missileshell.png");
    }
}
