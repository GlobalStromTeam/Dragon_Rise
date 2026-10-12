package com.redabysslucia.dragonrise_reforge.client.renderer.ammo;

import com.redabysslucia.dragonrise_reforge.client.model.ammo.IedModel;
import com.redabysslucia.dragonrise_reforge.entities.IedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 土制遥控炸弹渲染器 */
public class IedRenderer extends GeoEntityRenderer<IedEntity> {

    public IedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new IedModel());
        this.shadowRadius = 0.25F;
    }
}
