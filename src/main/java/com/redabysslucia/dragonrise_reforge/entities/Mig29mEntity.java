package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Mig29mEntity extends DragonriseVehicleBase {
    public Mig29mEntity(EntityType<? extends Mig29mEntity> type, Level level) { super(type, level); }
    @Override
    public double getMouseSensitivity() {
        return com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle ? 0.1 : 0.25;
    }

}
