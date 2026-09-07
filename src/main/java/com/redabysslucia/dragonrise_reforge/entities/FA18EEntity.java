package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

/**
 * F/A-18E 舰载战斗机实体：数据由 data/dragonrise_reforge/sbw/vehicles/fa18e.json 驱动
 * （OBB/座位/武器挂架/吊舱位置由 GeoOBBDataProvider 从模型骨骼生成），
 * 实体 id dragonrise_reforge:fa18e 供部署器/召唤生成，并用于模型/动画/贴图的自动解析。
 */
public class FA18EEntity extends DragonriseVehicleBase {

    public FA18EEntity(EntityType<FA18EEntity> type, Level world) {
        super(type, world);
    }


    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}
