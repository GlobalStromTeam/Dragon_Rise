package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class J20VTOLEntity extends VariableEngineVehicle {

    public J20VTOLEntity(EntityType<J20VTOLEntity> type, Level world) {
        super(type, world);
        this.setEngineTypeList(List.of(EngineType.AIRCRAFT, EngineType.HELICOPTER));
    }

//    @Override
//    public boolean useAircraftCamera(int seatIndex) {
//        return ModKeyMappings.FREE_CAMERA.isDown() && !ClientEventHandler.zoom;
//    }

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}
