package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.mob_guns.MobGunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
@SuppressWarnings("removal")
public class CV90Entity extends VehicleEntity {

        public CV90Entity(EntityType<CV90Entity> type, Level world) {
                super(type, world);
        }

public boolean countMissile(VehicleEntity vehicle, int ammo) {
        var gunData = vehicle.getGunData("Missile");
        if (gunData == null) {
            return false;
        } else {
            return gunData.ammo.get() < ammo;
        }
    }
}
