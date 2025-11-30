package com.redabysslucia.dragonrise_reforge.client.renderer.entity;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import com.redabysslucia.dragonrise_reforge.client.model.entity.M1A2SEPV2Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.T80Model;
import com.redabysslucia.dragonrise_reforge.client.model.entity.ZTZ99AModel;
import com.redabysslucia.dragonrise_reforge.entities.M1A2SEPV2Entity;
import com.redabysslucia.dragonrise_reforge.entities.ZTZ99AEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class M1A2SEPV2Renderer extends VehicleRenderer<M1A2SEPV2Entity> {
    public M1A2SEPV2Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager,new M1A2SEPV2Model());
    }
}
