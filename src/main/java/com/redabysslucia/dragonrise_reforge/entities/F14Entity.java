package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.atsuishio.superbwarfare.mixins.CameraMixin;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;


import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;

public class F14Entity extends DragonriseVehicleBase {

    public F14Entity(EntityType<F14Entity> type, Level world) {
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

    @Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }
}
