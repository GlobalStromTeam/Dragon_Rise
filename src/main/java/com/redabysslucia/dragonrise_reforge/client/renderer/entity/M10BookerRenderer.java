package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M10BookerModel;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M1A2SEPV2Model;
import com.redabysslucia.dragonrise_reforge.entities.M10BookerEntity;
import com.redabysslucia.dragonrise_reforge.entities.M1A2SEPV2Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;

public class M10BookerRenderer extends VehicleRenderer<M10BookerEntity> {
    public M10BookerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager,new M10BookerModel());
    }
}