package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class F4UEntity extends DragonriseVehicleBase {

    public F4UEntity(EntityType<F4UEntity> type, Level world) {
        super(type, world);
    }
@Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}
