package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.client.model.entity.VehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.MK19Entity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class MK19Model extends VehicleModel<MK19Entity> {
    @Override
    public @Nullable TransformContext<MK19Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "guanmiao" -> (bone, vehicle, state) -> {
                var player = Minecraft.getInstance().player;
                bone.setHidden(vehicle.getFirstPassenger() == player && (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON || ClientEventHandler.zoomVehicle));
            };
            case "missile" -> (bone, vehicle, state) -> bone.setHidden(!vehicle.getEntityData().get(MK19Entity.LOADED));
            default -> super.collectTransform(boneName);
        };
    }
}
