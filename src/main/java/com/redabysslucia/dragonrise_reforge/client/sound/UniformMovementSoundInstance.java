package com.redabysslucia.dragonrise_reforge.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/** A short mono movement fragment that follows its wearer and can be faded on stop/unequip. */
public final class UniformMovementSoundInstance extends AbstractTickableSoundInstance {
    private static final int FADE_TICKS = 3;
    private final Player wearer;
    private final float baseVolume;
    private final int startedAt;
    private int fadeRemaining = -1;

    public UniformMovementSoundInstance(Player wearer, SoundEvent sound, boolean crouching) {
        super(sound, SoundSource.PLAYERS, wearer.getRandom());
        this.wearer = wearer;
        this.startedAt = wearer.tickCount;
        this.baseVolume = crouching ? 0.55f : 1.0f;
        this.volume = baseVolume;
        this.pitch = 1.0f;
        updatePosition();
    }

    @Override
    public void tick() {
        if (wearer.isRemoved() || !wearer.isAlive()) {
            stop();
            return;
        }
        updatePosition();
        if (fadeRemaining >= 0) {
            if (--fadeRemaining <= 0) {
                stop();
                return;
            }
            volume = baseVolume * fadeRemaining / FADE_TICKS;
        }
    }

    private void updatePosition() {
        Minecraft client = Minecraft.getInstance();
        relative = wearer == client.player && client.getCameraEntity() == wearer;
        x = relative ? 0 : wearer.getX();
        y = relative ? 0 : wearer.getY() + 1;
        z = relative ? 0 : wearer.getZ();
    }

    public void fadeOut() {
        if (fadeRemaining < 0) fadeRemaining = FADE_TICKS;
    }

    public boolean fading() {
        return fadeRemaining >= 0;
    }

    public int age() {
        return wearer.tickCount - startedAt;
    }
}
