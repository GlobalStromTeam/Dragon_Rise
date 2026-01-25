package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.BMP3Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZBD04AModel;
import com.redabysslucia.dragonrise_reforge.entities.BMP3Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZBD04AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BMP3Renderer extends VehicleRenderer<BMP3Entity> {
        public BMP3Renderer(EntityRendererProvider.Context renderManager) {
                super(renderManager,new BMP3Model());
        }
}