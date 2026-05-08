package com.redabysslucia.dragonrise_reforge.client.model.entity.atmg;

import com.atsuishio.superbwarfare.entity.vehicle.TowEntity;
import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.redabysslucia.dragonrise_reforge.client.model.entity.DragonriseVehicleModel;
import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.DSHKEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class HJ8Model extends DragonriseVehicleModel<HJ8Entity> {

    @Override
    public @Nullable TransformContext<HJ8Entity> collectTransform(String boneName) {
        return switch (boneName) {
            case "guanmiao" -> (bone, vehicle, state) -> {
                var player = Minecraft.getInstance().player;
                bone.setHidden(vehicle.getFirstPassenger() == player && (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON || ClientEventHandler.zoomVehicle));
            };
            case "missile" -> (bone, vehicle, state) -> bone.setHidden(!vehicle.getEntityData().get(TowEntity.LOADED));
            default -> super.collectTransform(boneName);
        };
    }
}
