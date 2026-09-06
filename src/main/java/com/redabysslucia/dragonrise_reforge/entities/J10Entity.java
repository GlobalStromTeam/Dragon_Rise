package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.init.ModKeyMappings;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class J10Entity extends DragonriseVehicleBase {

    public J10Entity(EntityType<J10Entity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier()
                .custom((entity, source, damage) -> getSourceAngle(source, 0.25f) * damage * (getHealth() > 0.1f ? 0.4f : 0.05f));
    }
//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }


}
