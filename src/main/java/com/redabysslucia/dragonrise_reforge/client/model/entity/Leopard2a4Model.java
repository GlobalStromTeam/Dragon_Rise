package com.redabysslucia.dragonrise_reforge.client.model.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.Leopard2a4Entity;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class Leopard2a4Model extends DragonriseVehicleModel<Leopard2a4Entity> {
    @Override
    public boolean hideForTurretControllerWhileZooming() {
        return true;
    }
}
