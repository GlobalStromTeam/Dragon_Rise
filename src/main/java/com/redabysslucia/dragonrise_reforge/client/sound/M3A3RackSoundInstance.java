package com.redabysslucia.dragonrise_reforge.client.sound;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.redabysslucia.dragonrise_reforge.entities.M3A3Entity;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** One rack movement, attached to the vehicle; the recording supplies its own end. */
@OnlyIn(Dist.CLIENT)
public final class M3A3RackSoundInstance extends AbstractTickableSoundInstance {
    private final M3A3Entity vehicle;
    private final boolean raising;

    private M3A3RackSoundInstance(M3A3Entity vehicle, boolean raising, boolean inside) {
        super(event(raising, inside), SoundSource.PLAYERS, vehicle.level().getRandom());
        this.vehicle = vehicle;
        this.raising = raising;
        this.looping = false;
        this.delay = 0;
        this.volume = 1f;
        this.pitch = 1f;
        this.relative = inside;
        this.attenuation = inside ? Attenuation.NONE : Attenuation.LINEAR;
        updatePosition();
    }

    private static SoundEvent event(boolean raising, boolean inside) {
        if (raising) return inside ? ModSounds.M3A3_TOW_RAISE_1P.get() : ModSounds.M3A3_TOW_RAISE_3P.get();
        return inside ? ModSounds.M3A3_TOW_LOWER_1P.get() : ModSounds.M3A3_TOW_LOWER_3P.get();
    }

    public static M3A3RackSoundInstance play(M3A3Entity vehicle, boolean raising) {
        Minecraft client = Minecraft.getInstance();
        boolean inside = client.player != null && client.player.getVehicle() == vehicle
                && (client.options.getCameraType().isFirstPerson() || ClientEventHandler.zoomVehicle);
        M3A3RackSoundInstance sound = new M3A3RackSoundInstance(vehicle, raising, inside);
        client.getSoundManager().play(sound);
        return sound;
    }

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        int state = vehicle.getMissileState();
        if (vehicle.isRemoved() || !vehicle.isAlive() || client.level != vehicle.level()
                || (raising ? state == 0 || state == 3 : state == 1 || state == 2)) {
            stop();
            return;
        }
        updatePosition();
    }

    private void updatePosition() {
        this.x = relative ? 0 : vehicle.getX();
        this.y = relative ? 0 : vehicle.getY();
        this.z = relative ? 0 : vehicle.getZ();
    }

    public void stopPlayback() {
        stop();
    }
}
