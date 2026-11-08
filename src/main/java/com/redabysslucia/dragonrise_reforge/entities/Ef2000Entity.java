package com.redabysslucia.dragonrise_reforge.entities;

import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Ef2000Entity extends DragonriseVehicleBase {
    public Ef2000Entity(EntityType<? extends Ef2000Entity> type, Level level) { super(type, level); }
    @Override
    public double getMouseSensitivity() {
        return com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle ? 0.1 : 0.25;
    }

}
