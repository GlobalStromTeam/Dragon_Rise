package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class SU24MEntity extends DragonriseVehicleBase {

    public SU24MEntity(EntityType<SU24MEntity> type, Level world) {
        super(type, world);
    }
}
